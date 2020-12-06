#!/bin/bash
# uploads built artifacts to aresclient.org if branch is master and not a pr

if [ "$TRAVIS_BRANCH" != "master" ] || [ "$TRAVIS_PULL_REQUEST" = "true" ]; then
  exit 0
fi

for filename in $TRAVIS_BUILD_DIR/build/*.jar; do
    res=$(curl -F token=$UPLOAD_TOKEN -F file=@$filename https://aresclient.org/beta)
    echo $res
done
