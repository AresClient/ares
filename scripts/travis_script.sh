#!/bin/bash
# builds artifacts if on main branch, else just check

if [ "$TRAVIS_BRANCH" != "master" ] || [ "$TRAVIS_PULL_REQUEST" = "true" ]; then
  ./gradlew check
else
  ./gradlew build
fi