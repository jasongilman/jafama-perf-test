# jafama-perf-test

Implements a simple performance and accuracy test of the [Jafama](http://sourceforge.net/projects/jafama/) (Java Fast Math) library.

## Setup

Jafama isn't available in Maven so you have to install it manually. Download jafama.jar from http://sourceforge.net/projects/jafama/?source=typ_redirect

Install into local repo

```
mvn install:install-file -DartifactId=jafama -Dversion=2.1 -DgroupId=jafama -DgeneratePom=true -DcreateChecksum=true -Dpackaging=jar -Dfile=jafama.jar -DlocalRepositoryPath=maven_repository
```

## Running

1. Open a repl
2. `(use 'jafama-perf-test.core)`
3. `(evaluate-accuracy)`
4. `(evaluate-performance)`


## License

Copyright © 2015 Jason Gilman

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
