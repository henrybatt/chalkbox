# Creating an Engine

Engines in ChalkBox are responsible for taking a single student's submission,
running tests on it, and returning a JSON file containing the test results in
Gradescope format.

Currently, engines exist for both Java and Python, though engines do not
necessarily need to be based on a single language.

This guide will explain how
to create a ChalkBox engine, utilising the existing support framework in
ChalkBox to make engine development easier. 

## Main Engine Class

Each ChalkBox engine has a main class that extends the abstract class `Engine`.
These classes reside in the `chalkbox.engines` package.

A minimal example of an engine, DemoEngine, is as follows.

```java
package chalkbox.engines;

import chalkbox.api.collections.Collection;

public class DemoEngine extends Engine {
    @Override
    public void run() {
        System.out.println("Running DemoEngine");
        
        Collection submission = super.collect();
        
        // Operate on the submission here
        
        super.output(submission);
    }
}
```

The `Engine` abstract class provides a method to collect the student's
submission, `collect()`. Calling this method will return a `Collection` which
represents a single submission, including all the files submitted and a set of
metadata relevant to the submission.

A collection's metadata is stored internally as a JSON object. Initially, the
metadata includes the following keys:
- `root` : `String` path to the directory containing the submission
- `json` : `String` path to the JSON file where results will ultimately be
written

The `output()` method will write the contents of the metadata object, including
any test results added by the engine, to the output JSON file which will be read
by Gradescope.

A `Collection` also provides a `Bundle` to represent the submission directory,
and a `Bundle` to serve as a temporary directory during the engine's operation. 
A `Bundle` is essentially a wrapper for a directory, providing helpful methods
to retrieve files in that directory.

## Configuration and Loading

Configuration options for engines are specified in a YAML file. The path to
this file is passed as a command line argument when running the ChalkBox JAR.

The format for an engine's configuration file is as follows.

```yaml
engine: chalkbox.engines.DemoEngine
---
courseCode: ABCD1234
assignment: assignment_1
submission: /path/to/submission/dir/
outputFile: /path/to/results.json
```

When ChalkBox is run with a given configuration file, the `EngineLoader` class
reads the first document in the file (lines before the `---`) to determine
which engine to invoke on the submission.

The engine loader then instantiates the specified engine class.

TODO explain how configuration options are set via JavaBeans

## Engine-specific Functionality

TODO talk about individual packages for engine-specific classes