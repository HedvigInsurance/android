import SwiftUI
import HedvigShared

struct ContentView: View {
    var body: some View {
        VStack {
            ClaimChatViewController()
        }
        .padding()
    }
}

struct ClaimChatViewController: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return ClaimChatViewControllerKt.ClaimChatViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}

#Preview {
    ContentView()
}
