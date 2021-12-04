#!/bin/bash
# uploads build artifacts to aresclient.org if branch is master and not a pr

if [ "$TRAVIS_BRANCH" != "master" ] || [ "$TRAVIS_PULL_REQUEST" = "true" ]; then
  exit 0
fi

FORGE_PATH=""
FABRIC_PATH=""
FABRIC_16_PATH=""
FABRIC_17_PATH=""

for filename in $TRAVIS_BUILD_DIR/build/*.jar; do
  if grep -q "forge" <<< "$filename"; then
    FORGE_PATH="$filename"
  fi
  if grep -q "fabric" <<< "$filename" && grep -qv "1.16" <<< "$filename" && grep -qv "1.17" <<< "$filename"; then
    FABRIC_PATH="$filename"
  fi
  if grep -q "fabric-1.16" <<< "$filename"; then
    FABRIC_16_PATH="$filename"
  fi
  if grep -q "fabric-1.17" <<< "$filename"; then
      FABRIC_17_PATH="$filename"
    fi
done

COMMITS=$(git log $TRAVIS_COMMIT_RANGE --oneline | tac)
if grep -qv "NO-BETA" <<< "$COMMITS"; then
   if [ $FORGE_PATH != "" ] && [ $FABRIC_PATH  != "" ] && [ $FABRIC_16_PATH  != "" ]  && [ $FABRIC_17_PATH  != "" ]; then
     echo $(curl -F token=$UPLOAD_TOKEN -F message="$COMMITS" -F forge=@$FORGE_PATH -F fabric=@$FABRIC_PATH -F fabric_16=@$FABRIC_16_PATH -F fabric_17=@$FABRIC_17_PATH https://aresclient.org/beta)
   else
     echo "Couldn't find path to forge and fabric builds!"
   fi
else
  echo "Skipped beta upload!"
fi
