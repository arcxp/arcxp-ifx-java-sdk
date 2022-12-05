#!/bin/bash -e

mvn install
mv ~/.m2/repository/com/arcpublishing/platform/sdk/arc-platform-sdk/1.0-SNAPSHOT/arc-platform-sdk-1.0-SNAPSHOT-jar-with-dependencies.jar ~/.m2/repository/com/arcpublishing/platform/sdk/arc-platform-sdk/1.0-SNAPSHOT/arc-platform-sdk-1.0-SNAPSHOT.jar;