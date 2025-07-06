// swift-tools-version:5.3

import PackageDescription

let package = Package(
    name: "umbrella",
    platforms: [
        .iOS(.v14),
    ],
    products: [
        .library(
            name: "umbrella",
            targets: ["umbrella"]
        )
    ],
    targets: [
        .binaryTarget(
            name: "umbrella",
            url: "https://github.com/HedvigInsurance/umbrella/releases/download/v0.0.3/umbrella.xcframework.zip",
            checksum: "35fc65d3a1e912539ec609e4f564b885dca4e56615da3bb44d5f93549d394a72"
        )
    ]
)