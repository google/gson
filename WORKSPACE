workspace(name = "com_google_gson")

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:java.bzl", "java_import_external")

# Apache 2.0
http_archive(
    name = "rules_java",
    sha256 = "703aab6b54a81248afb7f5c5ce6189f5711441ee5e2c4d2ef200ce530e494630",
    strip_prefix = "rules_java-0.1.0",
    urls = [
        "https://github.com/bazelbuild/rules_java/archive/0.1.0.tar.gz",
    ],
)

java_import_external(
    name = "junit_junit",
    jar_sha256 = "59721f0805e223d84b90677887d9ff567dc534d7c502ca903c0c2b17f05c116a",
    jar_urls = [
        "http://bazel-mirror.storage.googleapis.com/repo1.maven.org/maven2/junit/junit/4.12/junit-4.12.jar",
        "http://maven.ibiblio.org/maven2/junit/junit/4.12/junit-4.12.jar",
        "http://repo1.maven.org/maven2/junit/junit/4.12/junit-4.12.jar",
    ],
)

load("@rules_java//java:repositories.bzl", "rules_java_dependencies", "rules_java_toolchains")
rules_java_dependencies()
rules_java_toolchains()
