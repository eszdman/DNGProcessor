cmake_minimum_required(VERSION 3.6)

project(tinydngJNI)

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

set(SRC_DIR ${CMAKE_CURRENT_SOURCE_DIR})

include(loadDeps.cmake)

include_directories(deps/tinydng)
include_directories(inc)
link_libraries(log)
add_library(${CMAKE_PROJECT_NAME} SHARED
        ${SRC_DIR}/dngLoader.cpp)
