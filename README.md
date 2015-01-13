score 
=====

score is a general-purpose orchestration engine which is process-based, embeddable, lightweight, scalable and multilingual.

[![Build Status](https://travis-ci.org/openscore/score.svg?branch=master)](https://travis-ci.org/openscore/score)


score is the core engine for running workflows, 
it supports different workflows  languages (DSL) by a pluggable compiler approach.
Where adding a new workflow DSL is adding a new Compiler that will translate the DSL(xml, yaml, etc ..) to a generic workflow representation we call ExecutionPlan.

For example of such Compiler and DSL, look at [project slang](https://github.com/openscore/score-language).
