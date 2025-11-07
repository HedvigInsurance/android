import SwiftUI
import HedvigShared

@main
struct umbrella_consumerApp: App {
    init() {
        Main_nativeKt.doInitKoin(getAuthToken: { "eyJraWQiOiJCSnd5VGNnek5WUmpmX0VuZjFKUFgxd3lrUjZSMElOTXRiR015UkduVkhNIiwiYWxnIjoiUlMyNTYifQ.eyJpbXBlcnNvbmF0ZWQtYnkiOiJhZG1fNWNmOTA4ZjAtMGZhMi00ZGE0LWExNzAtMjcyNjQwNTc3MDVhIiwic3ViIjoibWVtXzQ1NTQ2OTk5NyIsImV4cCI6MTc2MjUyNDEwOSwiaWF0IjoxNzYyNTIwNTA5fQ.UgiwCZpXR7ZyNM6usphJzCiKmuIvzI5UuEliHRtsrJ99aTv4rCqby1O8ZOjd-HS-vIqr-_If3n9FXDm2v-dsoHRSnThUQyWwRN9iXldql3uarlbHrVX5k9J8wdU29onoeKcOHhKDZFYUgXO4H6IqcaSVlA2L4G5IqjL44cClLZMQMhIu53YNTxg_51rgC14WEpfhi8YxlEqiTFQN54lrbff0-VN7ndxZegOOLmOz3uj_rmZ_8K0JHWeGVsryCERELp1gUfV4OrHgxtmrcER_xPGTgRDdmAkg7XFWcFGhAXbfmJhE5TbE19zwMDR5g8AZeGEjdq-ON3Ket_cXtoEsZg" })
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
