score 
=====

The CloudSlang Orchestration Engine (Score) is a general-purpose orchestration engine which is process-based, embeddable, lightweight, scalable and multilingual.

[![Build Status](https://travis-ci.org/CloudSlang/score.svg?branch=master)](https://travis-ci.org/CloudSlang/score)
[![Maintainability](https://api.codeclimate.com/v1/badges/45981e8ab04cec2b9bbb/maintainability)](https://codeclimate.com/github/CloudSlang/score/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/45981e8ab04cec2b9bbb/test_coverage)](https://codeclimate.com/github/CloudSlang/score/test_coverage)


Score is the core engine for running workflows. It supports multiple workflow  languages (DSL) using a pluggable compiler approach. Adding a new workflow DSL requires adding a new compiler that will translate the DSL (written in xml, yaml, etc.) to a generic workflow representation called an ExecutionPlan.

***For an example compiler and DSL see the*** [CloudSlang project](https://github.com/cloudslang/cloud-slang).


Latest Maven Central release versions
-------------------------------------

| Module | Release |
| ----- | ----- |
| score-parent | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-parent/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-parent)
| engine | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/engine/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/engine)
| data | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/data/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/data)
| score-data-api | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-data-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-data-api)
| score-api | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-api)
| score-facade | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-facade/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-facade)
| orchestrator | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/orchestrator/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/orchestrator)
| score-orchestrator-api | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-orchestrator-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-orchestrator-api)
| node | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/node/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/node)
| score-node-api | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-node-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-node-api)
| queue |  [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/queue/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/queue)
| score-queue-api | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-queue-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-queue-api)
| score-orchestrator-impl | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-orchestrator-impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-orchestrator-impl)
| score-queue-impl | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-queue-impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-queue-impl)
| score-node-impl | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-node-impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-node-impl)
| score-engine-jobs | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-engine-jobs/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-engine-jobs)
| score-data-impl | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-data-impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-data-impl)
| worker | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/worker/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/worker)
| worker-execution | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/worker-execution/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/worker-execution)
| score-worker-execution-api | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-worker-execution-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-worker-execution-api)
| worker-manager | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/worker-manager/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/worker-manager)
| score-worker-manager-api | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-worker-manager-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-worker-manager-api)
| score-worker-execution-impl | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-worker-execution-impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-worker-execution-impl)
| score-worker-manager-impl | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang.content/cs-xml/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang.content/cs-xml)
| package | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/package/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/package)
| score-worker | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-worker/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-worker)
| score-all | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-all/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-all)
| score-samples | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-samples/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-samples)
| control-action-samples | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/control-action-samples/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/control-action-samples)
| score-tests | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-tests/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/score-tests)
| hello-score | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/hello-score/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/hello-score)
| dependency-management | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/dependency-management/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/dependency-management)
| dependency-management-api | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/dependency-management-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/dependency-management-api)
| dependency-management-impl | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/dependency-management-impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/dependency-management-impl)
| runtime-management | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/runtime-management/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/runtime-management)
| runtime-management-api | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/runtime-management-api/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/runtime-management-api)
| runtime-management-impl | [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/runtime-management-impl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.cloudslang/runtime-management-impl)


#### Building and Testing from Source

The Score project uses Maven to build and test.

###### Prerequisites:

1. Maven version >=3.0.3
2. Java JDK version >=7

###### Steps:

1. ```git clone``` the source code
2. ```mvn clean install```
