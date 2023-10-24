include(FetchContent)

file(MAKE_DIRECTORY deps)

FetchContent_Declare(tinydngGit
  GIT_REPOSITORY "https://github.com/eszdman/tinydng.git"
  GIT_TAG "origin/release"   # it's much better to use a specific Git revision or Git tag for reproducibility
  SOURCE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/deps/tinydng"
)
FetchContent_MakeAvailable(tinydngGit)

