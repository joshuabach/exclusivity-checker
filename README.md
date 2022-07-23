# Exclusivity Checker

A common problem when programming is aliasing.
This can result in a multitude of errors, often hard to track down.

The Exclusivity Checker guarantees, at compile time, that any reference 
annotated as such is not aliased to another mutable reference.

## How to run the checker

First, publish the checker to your local Maven repository by running
`./gradlew publishToMavenLocal` in this repository.

Then, if you use Gradle, add the following to the `build.gradle` file in
the project you wish to type-check (using Maven is similar):

```
repositories {
    mavenLocal()
    mavenCentral()
}
dependencies {
    annotationProcessor 'org.checkerframework:exclusivity-checker'
}
```

Now, when you build your project, the Exclusivity Checker will also run,
informing you of any potential errors related to exclusvity.


## How to specify your code

At compile time, the ExclusivityChecker estimates what values the program
may compute at run time.  It issues a warning if the program contains references 
that are potentially aliased, but are expected not to.
It works via a technique called pluggable typechecking.

You need to specify the contracts of methods and fields in your code --
that is, their requirements and their guarantees.  The ExclusivityChecker
ensures that your code is consistent with the contracts, and that the
contracts guarantee exclusivity.

You specify your code by writing *qualifiers* such as `@ExclMut`
on types, to indicate more precisely what values the type represents.
Here are the type qualifiers that are supported by the ExclusivityChecker:

`@ReadOnly`:
Object may not be mutated and naturally only be copied as `@ReadOnly`.
Aliases to the same object may be of any type.

`@ShrMut`:
Object may be mutated and reference can be copied without restriction.
Mutable aliases to the same object might exist. 
This is the default type, so you usually do not need to write it.

`@ExclMut`:
Object may be mutated, but the reference may only be copied as `@ReadOnly`.
Any alias to the same object ist at most `@ReadOnly`.

`@Immutable`:
Object may not be mutated, but the reference may be copied without restriction.
Any alias to the same object is at most `@Immutable`.


## How to build the checker

Run these commands from the top-level directory.

`./gradlew build`: build the checker

`./gradlew publishToMavenLocal`: publish the checker to your local Maven repository.
This is useful for testing before you publish it elsewhere, such as to Maven Central.


## More information

This checker has been developed as part of my Master's Thesis at KIT
which can be accessed [on GitLab](https://git.scc.kit.edu/uwdkl/mt-ptmdsj).
It should be considered a research project and is not yet suited for production.

The Exclusivity Checker is built upon the Checker Framework.  Please see
the [Checker Framework Manual](https://checkerframework.org/manual/) for
more information about using pluggable type-checkers, including this one.
