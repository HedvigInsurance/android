import SwiftUI
import HedvigShared

struct ContentView: View {
    var body: some View {
        HelpCenterViewController().ignoresSafeArea(.all)
    }
}

struct HelpCenterViewController: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return HelpCenterViewControllerKt.HelpCenterViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

#Preview {
    ContentView()
}
