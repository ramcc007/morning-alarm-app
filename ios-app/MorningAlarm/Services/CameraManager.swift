import AVFoundation
import Vision
import Combine
import UIKit

/// Owns the front-camera capture session and runs Vision body-pose detection
/// on every frame, forwarding observations to whichever `RepDetector` is active.
///
/// iOS only allows camera access while the app is in the foreground, so this
/// is only ever running while `AlarmRingingView` (or its camera subview) is on screen.
final class CameraManager: NSObject, ObservableObject {
    let session = AVCaptureSession()

    @Published var isBodyDetected: Bool = false
    @Published var authorizationDenied: Bool = false

    private let videoOutput = AVCaptureVideoDataOutput()
    private let processingQueue = DispatchQueue(label: "com.morningalarm.camera.processing")
    private let poseRequest = VNDetectHumanBodyPoseRequest()

    weak var activeDetector: RepDetector?

    func configureAndStart() {
        AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
            guard let self else { return }
            DispatchQueue.main.async {
                if granted {
                    self.setUpSession()
                } else {
                    self.authorizationDenied = true
                }
            }
        }
    }

    private func setUpSession() {
        processingQueue.async { [weak self] in
            guard let self else { return }
            self.session.beginConfiguration()
            self.session.sessionPreset = .high

            if let device = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .front),
               let input = try? AVCaptureDeviceInput(device: device),
               self.session.canAddInput(input) {
                self.session.addInput(input)
            }

            self.videoOutput.setSampleBufferDelegate(self, queue: self.processingQueue)
            self.videoOutput.videoSettings = [kCVPixelBufferPixelFormatTypeKey as String: Int(kCVPixelFormatType_32BGRA)]
            if self.session.canAddOutput(self.videoOutput) {
                self.session.addOutput(self.videoOutput)
            }
            self.videoOutput.connection(with: .video)?.videoRotationAngle = 90 // portrait, front camera

            self.session.commitConfiguration()
            self.session.startRunning()
        }
    }

    func stop() {
        processingQueue.async { [weak self] in
            self?.session.stopRunning()
        }
    }
}

extension CameraManager: AVCaptureVideoDataOutputSampleBufferDelegate {
    func captureOutput(
        _ output: AVCaptureOutput,
        didOutput sampleBuffer: CMSampleBuffer,
        from connection: AVCaptureConnection
    ) {
        guard let pixelBuffer = CMSampleBufferGetImageBuffer(sampleBuffer) else { return }

        let handler = VNImageRequestHandler(cvPixelBuffer: pixelBuffer, orientation: .up, options: [:])
        do {
            try handler.perform([poseRequest])
            guard let observation = poseRequest.results?.first else {
                DispatchQueue.main.async { self.isBodyDetected = false }
                return
            }
            DispatchQueue.main.async { self.isBodyDetected = true }
            activeDetector?.process(observation: observation)
        } catch {
            // Vision occasionally fails on a malformed frame; just skip it.
        }
    }
}
