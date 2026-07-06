import SwiftUI

/// Centralized visual language so every screen feels like one product.
enum Theme {
    static let background = Color(red: 0.06, green: 0.07, blue: 0.1)
    static let surface = Color(red: 0.11, green: 0.12, blue: 0.16)
    static let surfaceElevated = Color(red: 0.15, green: 0.16, blue: 0.21)

    static let sunrise = LinearGradient(
        colors: [Color(red: 1.0, green: 0.42, blue: 0.31), Color(red: 1.0, green: 0.69, blue: 0.2)],
        startPoint: .topLeading, endPoint: .bottomTrailing
    )
    static let energy = LinearGradient(
        colors: [Color(red: 0.98, green: 0.34, blue: 0.55), Color(red: 1.0, green: 0.55, blue: 0.26)],
        startPoint: .leading, endPoint: .trailing
    )
    static let success = Color(red: 0.31, green: 0.85, blue: 0.55)

    static let textPrimary = Color.white
    static let textSecondary = Color.white.opacity(0.6)

    static let cardCorner: CGFloat = 20
    static let controlCorner: CGFloat = 14
}

struct PrimaryButtonStyle: ButtonStyle {
    var isEnabled: Bool = true

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.system(size: 17, weight: .semibold, design: .rounded))
            .foregroundStyle(.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .background(isEnabled ? Theme.energy : LinearGradient(colors: [.gray, .gray], startPoint: .leading, endPoint: .trailing))
            .clipShape(RoundedRectangle(cornerRadius: Theme.controlCorner, style: .continuous))
            .scaleEffect(configuration.isPressed ? 0.97 : 1)
            .opacity(configuration.isPressed ? 0.9 : 1)
            .animation(.easeOut(duration: 0.15), value: configuration.isPressed)
    }
}

struct CardBackground: ViewModifier {
    func body(content: Content) -> some View {
        content
            .background(Theme.surface)
            .clipShape(RoundedRectangle(cornerRadius: Theme.cardCorner, style: .continuous))
    }
}

extension View {
    func cardStyle() -> some View { modifier(CardBackground()) }
}
