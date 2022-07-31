## hanalytics engineering noop

This module exists as something for app to depend on which exposes the same API as the
hanalytics-engineering does, without giving any of the functionality.
This is to let :app depend on hanalytics-engineering and use that as a debug implementation while 
still being able to resolve all symbols in the release version too. 