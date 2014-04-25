# lein-ubersource

A [Leiningen](https://github.com/technomancy/leiningen) plugin to download source code for all (transitive) dependencies of a project.

The original motivation was to make it really simple to get all of the upstream Java source code for projects that have Java dependencies.  This makes it easy to, e.g., grep through the entire source tree to see if a certain class is used (which can be extremely handy when trying to determine whether or not an Oracle CVE affects your project).

__Leiningen__ ([via Clojars](https://clojars.org/lein-ubersource))

Put the following into the `:plugins` vector of the `:user` profile in your `~/.lein/profiles.clj`:

```clojure
[lein-ubersource "0.1.0"]
```

This plugin is destined for Leiningen >= 2.0.0.

## Usage

Simply run `lein ubersource`, and the plugin will do the following:

* Determine the list of all of the dependencies (including transitive) for your current leiningen project
* Create a directory called `target/ubersource`
* For each dependency:
   * Check all of your project's `repositories` for a `sources` jar for that dependency
   * If a `sources` jar is not available, fall back to the "main" jar for that dependency (for clojure projects, this likely *is* the "source" jar)
   * Unzip the jar file into a directory inside of `target/ubersource`.

## License

Copyright &copy; 2014 Puppet Labs

Distributed under the Apache License, v2.  See [LICENSE](./LICENSE)
