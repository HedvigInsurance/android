import SwiftUI
import HedvigShared

struct ContentView: View {
    @State private var presented: Destination?

    enum Destination: Identifiable {
        case helpCenter
        case puppyGuide
        var id: Self { self }
    }

    var body: some View {
        NavigationStack {
            List {
                Button("Help Center") { presented = .helpCenter }
                Button("Puppy Guide") { presented = .puppyGuide }
            }
            .navigationTitle("Umbrella consumer")
        }
        .fullScreenCover(item: $presented) { destination in
            switch destination {
            case .helpCenter:
                HelpCenterScreen(onDismiss: { presented = nil })
                    .ignoresSafeArea(.all)
            case .puppyGuide:
                PuppyGuideStack(onDismiss: { presented = nil })
                    .ignoresSafeArea(.all)
            }
        }
    }
}

private struct HelpCenterScreen: UIViewControllerRepresentable {
    let onDismiss: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        return HelpCenterViewControllerKt.HelpCenterViewController(
            onNavigateUp: onDismiss,
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

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

private struct PuppyGuideStack: View {
    let onDismiss: () -> Void
    @State private var path: [String] = []

    var body: some View {
        NavigationStack(path: $path) {
            PuppyGuideScreen(
                onNavigateUp: onDismiss,
                onNavigateToArticle: { storyName in path.append(storyName) }
            )
            .ignoresSafeArea(.all)
            .toolbar(.hidden, for: .navigationBar)
            .navigationDestination(for: String.self) { storyName in
                PuppyArticleScreen(
                    storyName: storyName,
                    navigateUp: { if !path.isEmpty { path.removeLast() } }
                )
                .ignoresSafeArea(.all)
                .toolbar(.hidden, for: .navigationBar)
            }
        }
    }
}

private struct PuppyGuideScreen: UIViewControllerRepresentable {
    let onNavigateUp: () -> Void
    let onNavigateToArticle: (String) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        return PuppyGuideViewControllersKt.PuppyGuideViewController(
            onNavigateUp: onNavigateUp,
            onNavigateToArticle: onNavigateToArticle
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

private struct PuppyArticleScreen: UIViewControllerRepresentable {
    let storyName: String
    let navigateUp: () -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        return PuppyGuideViewControllersKt.PuppyArticleViewController(
            storyName: storyName,
            navigateUp: navigateUp
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

#Preview {
    ContentView()
}
