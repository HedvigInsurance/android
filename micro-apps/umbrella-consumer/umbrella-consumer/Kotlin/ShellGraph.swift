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
            demoMode: ShellDemoMode(),
            networkLogPolicy: networkLogPolicy()
        )
    }

    /// HTTP bodies and GraphQL response data reach the log only in a Debug build launched with
    /// `-kotlinLogNetworkBodies YES`.
    private static func networkLogPolicy() -> Network_clientsNetworkLogPolicy {
        #if DEBUG
        let logsBodies = ShellDebug.logNetworkBodies
        #else
        let logsBodies = false
        #endif
        return Network_clientsNetworkLogPolicy(logsHttpBodies: logsBodies, logsGraphqlData: logsBodies)
    }
}
