# ChalkBox ![Gradle Build](https://github.com/UQTools/chalkbox/workflows/Gradle%20Build/badge.svg)

![ChalkBox Logo](https://github.com/UQTools/chalkbox/raw/master/docs/_static/images/logo/chalkbox.png)

## Overview
ChalkBox is a tool for processing and marking programming assignment
submissions.

Common use cases include running automated tests and static analysis tools on
submitted code.

### Key Features

- **Integrates with Gradescope**

  ChalkBox has been redesigned to work with the
  [Gradescope](https://www.gradescope.com/) autograder platform.
  ChalkBox runs on a single submission, so students can receive instant feedback
  every time they submit.

- **Easy to use**

  Configuration for ChalkBox and its engines is specified in human-readable
  YAML, but can be easily generated using the web-based interface of 
  [Quickscope](https://github.com/uqtools/quickscope).

- **Extensible**

  A simple yet flexible Engine API allows the creation of new engines for
  different programming languages and use cases.

## Documentation

Javadoc for the source code of ChalkBox and guides for working with and
extending ChalkBox can be found at
[chalkbox.readthedocs.io](https://chalkbox.readthedocs.io).

## Copyright

ChalkBox is copyright Brae Webb, Emily Bennett, Ella de Lore, Max Miller,
Evan Hughes and Nicholas Lambourne.

The ChalkBox logo is copyright Anna Truffet.
