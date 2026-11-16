@preconcurrency import HedvigShared

@MainActor
enum ShellGraph {
    /// Builds the Kotlin graph. Call once per process, on the main thread.
    static func makeHolder(accessTokenFetcher: ShellAccessTokenFetcher) -> IosGraphHolder {
        IosGraphHolderKt.createIosGraphHolder(
            appBuildConfig: ShellAppBuildConfig(),
            accessTokenFetcher: accessTokenFetcher,
            deviceIdFetcher: ShellDeviceIdFetcher(),
            languageStorage: ShellLanguageStorage(),
            demoMode: ShellDemoMode()
        )
    }
}
