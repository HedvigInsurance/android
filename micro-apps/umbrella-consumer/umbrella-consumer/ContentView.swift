import HedvigShared
import SwiftUI

// The umbrella no longer exports any Compose screen (the puppy guide moved to native iOS), so this
// harness only checks that the non-UI exports link and are callable from Swift. See
// IosInterfaces.swift for the Kotlin interfaces it implements.
struct ContentView: View {
    var body: some View {
        NavigationStack {
            List {
                Text("HedvigShared linked")
            }
            .navigationTitle("Umbrella consumer")
        }
    }
}

#Preview {
    ContentView()
}
