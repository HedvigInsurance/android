/**
 * If the :umbrella project contains no source of its own, the `assembleUmbrellaReleaseXCFramework` task thinks there
 * are no inputs to work on so it skips building the XCFramework. This exists to solve this very problem.
 */
@Suppress("unused")
class Umbrella
