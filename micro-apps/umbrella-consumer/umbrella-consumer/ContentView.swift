import SwiftUI
import HedvigShared

struct ContentView: View {
    var body: some View {
        VStack {
            HelpCenterViewController()
        }
        .padding()
    }
}

struct HelpCenterViewController: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
//        return HelpCenterViewControllerKt.HelpCenterViewController()
        return UIViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

#Preview {
    ContentView()
}
