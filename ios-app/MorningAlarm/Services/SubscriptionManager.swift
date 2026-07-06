import Foundation
import StoreKit

/// Product identifier for the single monthly subscription. The 3-day free
/// trial is configured as an introductory offer on this product in App
/// Store Connect (and mirrored in `Configuration/Products.storekit` for
/// local Xcode testing) - it is not something the client sets in code.
enum ProductID {
    static let monthly = "com.morningalarm.app.monthly"
}

@MainActor
final class SubscriptionManager: ObservableObject {
    static let shared = SubscriptionManager()

    @Published private(set) var products: [Product] = []
    @Published private(set) var isSubscribed: Bool = false
    @Published private(set) var isLoadingProducts = false
    @Published var lastErrorMessage: String?

    private var updateListenerTask: Task<Void, Never>?

    private init() {
        updateListenerTask = listenForTransactionUpdates()
        Task {
            await loadProducts()
            await refreshEntitlements()
        }
    }

    deinit {
        updateListenerTask?.cancel()
    }

    var monthlyProduct: Product? {
        products.first { $0.id == ProductID.monthly }
    }

    /// Human readable trial description, e.g. "3-day free trial, then ₹50.00/month".
    var trialDescription: String? {
        guard let subscription = monthlyProduct?.subscription,
              let introOffer = subscription.introductoryOffer,
              introOffer.paymentMode == .freeTrial else {
            return nil
        }
        let days = introOffer.period.value
        return "\(days)-day free trial, then \(monthlyProduct?.displayPrice ?? "₹50.00")/month"
    }

    func loadProducts() async {
        isLoadingProducts = true
        defer { isLoadingProducts = false }
        do {
            products = try await Product.products(for: [ProductID.monthly])
        } catch {
            lastErrorMessage = "Couldn't load subscription info. Check your connection and try again."
        }
    }

    func purchaseMonthly() async {
        guard let product = monthlyProduct else {
            lastErrorMessage = "Subscription is not available right now."
            return
        }
        do {
            let result = try await product.purchase()
            switch result {
            case .success(let verification):
                let transaction = try checkVerified(verification)
                await transaction.finish()
                await refreshEntitlements()
            case .userCancelled:
                break
            case .pending:
                lastErrorMessage = "Your purchase is pending approval."
            @unknown default:
                break
            }
        } catch {
            lastErrorMessage = "Purchase failed: \(error.localizedDescription)"
        }
    }

    func restorePurchases() async {
        do {
            try await AppStore.sync()
            await refreshEntitlements()
        } catch {
            lastErrorMessage = "Restore failed: \(error.localizedDescription)"
        }
    }

    func refreshEntitlements() async {
        var subscribed = false
        for await result in Transaction.currentEntitlements {
            guard let transaction = try? checkVerified(result) else { continue }
            if transaction.productID == ProductID.monthly, transaction.revocationDate == nil {
                subscribed = true
            }
        }
        isSubscribed = subscribed
    }

    private func listenForTransactionUpdates() -> Task<Void, Never> {
        Task.detached { [weak self] in
            for await update in Transaction.updates {
                guard let self, let transaction = try? await self.checkVerified(update) else { continue }
                await transaction.finish()
                await self.refreshEntitlements()
            }
        }
    }

    private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .unverified:
            throw StoreError.failedVerification
        case .verified(let safe):
            return safe
        }
    }

    private enum StoreError: Error {
        case failedVerification
    }
}
