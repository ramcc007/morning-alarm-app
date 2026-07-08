package com.wakerep.app.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Owns the front-camera CameraX pipeline and runs ML Kit's on-device,
 * real-time pose detector on every analyzed frame, forwarding results to
 * whichever [RepDetector] is currently active.
 *
 * Android only allows camera access while the hosting activity is at least
 * started, which is exactly the window `AlarmRingingActivity` is on screen for.
 */
@OptIn(ExperimentalGetImage::class)
class PoseCameraController(private val context: Context) {

    var activeDetector: RepDetector? = null
    var onBodyDetectedChanged: ((Boolean) -> Unit)? = null

    private val poseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null

    fun bindToLifecycle(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            cameraProvider = provider

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(analysisExecutor, ::processFrame) }

            provider.unbindAll()
            provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                preview,
                analysis
            )
        }, ContextCompat.getMainExecutor(context))
    }

    private fun processFrame(imageProxy: androidx.camera.core.ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                val hasBody = pose.allPoseLandmarks.isNotEmpty()
                onBodyDetectedChanged?.invoke(hasBody)
                if (hasBody) activeDetector?.process(pose)
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    fun unbind() {
        cameraProvider?.unbindAll()
    }

    fun shutdown() {
        unbind()
        poseDetector.close()
        analysisExecutor.shutdown()
    }
}
