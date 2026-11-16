import CoreImage
import CoreImage.CIFilterBuiltins
import UIKit

enum QrCodeImage {
    /// A QR code of `side` pixels, drawn without smoothing.
    static func make(_ data: String, side: CGFloat) -> UIImage? {
        let filter = CIFilter.qrCodeGenerator()
        filter.message = Data(data.utf8)
        filter.correctionLevel = "M"
        guard let output = filter.outputImage, output.extent.width > 0 else { return nil }
        let scale = (side / output.extent.width).rounded(.down)
        let scaled = output.transformed(by: CGAffineTransform(scaleX: scale, y: scale))
        guard let cgImage = CIContext().createCGImage(scaled, from: scaled.extent) else { return nil }
        return UIImage(cgImage: cgImage)
    }
}
