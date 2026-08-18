@preconcurrency import HedvigShared
import SwiftUI

struct ContentView: View {
    @State private var presented: Destination?

    enum Destination: Identifiable {
        case puppyGuide
        var id: Self { self }
    }

    var body: some View {
        NavigationStack {
            List {
                Button("Puppy Guide") { presented = .puppyGuide }
            }
            .navigationTitle("Umbrella consumer")
        }
        .fullScreenCover(item: $presented) { destination in
            switch destination {
            case .puppyGuide:
                PuppyGuideStack(onDismiss: { presented = nil })
                    .ignoresSafeArea(.all)
            }
        }
    }
}

// The umbrella-exported Compose screens drive the system swipe-back gesture and the
// nav bar scroll-edge appearance through these hooks. This test harness hosts them in a
// plain NavigationStack without that infra, so a no-op controller is enough.
private final class NoopSwipeBackController: NSObject, IosSwipeBackController {
    func setSwipeBackEnabled(isEnabled: Bool) {}
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
            onNavigateToArticle: onNavigateToArticle,
            swipeBackController: NoopSwipeBackController(),
            onScrollOffsetChanged: { _ in }
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
            navigateUp: navigateUp,
            swipeBackController: NoopSwipeBackController(),
            onScrollOffsetChanged: { _ in }
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

#Preview {
    ContentView()
}
