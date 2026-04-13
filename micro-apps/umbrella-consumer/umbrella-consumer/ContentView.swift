import SwiftUI
import HedvigShared

struct ContentView: View {
    var body: some View {
        HelpCenterViewController().ignoresSafeArea(.all)
    }
}

struct HelpCenterViewController: UIViewControllerRepresentable {
    @Environment(\.dismiss) private var dismiss

    func makeUIViewController(context: Context) -> UIViewController {
        return HelpCenterViewControllerKt.HelpCenterViewController(
            onNavigateUp: { dismiss() },
            onNavigateToInbox: {},
            onNavigateToNewConversation: {},
            onNavigateToQuickLink: { _ in },
            openUrl: { url in
                if let nsUrl = URL(string: url) {
                    UIApplication.shared.open(nsUrl)
                }
            },
            tryToDialPhone: { number in
                let cleaned = number.components(separatedBy: .whitespaces).joined()
                if let url = URL(string: "tel://\(cleaned)") {
                    UIApplication.shared.open(url)
                }
            }
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

#Preview {
    ContentView()
}
