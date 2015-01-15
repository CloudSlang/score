score 
=====

score is a general-purpose orchestration engine which is process-based, embeddable, lightweight, scalable and multilingual.

[![Build Status](https://travis-ci.org/openscore/score.svg?branch=master)](https://travis-ci.org/openscore/score)


score is the core engine for running workflows. It supports multiple workflow  languages (DSL) using a pluggable compiler approach. Adding a new workflow DSL requires adding a new compiler that will translate the DSL (written in xml, yaml, etc.) to a generic workflow representation called an ExecutionPlan.

For an example compiler and DSL see the [Slang project](https://github.com/openscore/score-language).

#### Building and Testing from Source

The score project uses Maven to build and test.

1. ```git clone``` the source code
2. ```mvn clean install```
