# Report for assignment 3

This is a template for your report. You are free to modify it as needed.
It is not required to use markdown for your report either, but the report
has to be delivered in a standard, cross-platform format.

## Project

Name: Gson

URL: https://github.com/google/gson

Gson is a Java library used to convert Java objects into their JSON representation and convert JSON string to an equivelent Java object. Gson can work with arbitrary Java objects including pre-existing objects you do not have the source-code of.

## P+
For P+ we aim for the following points
* 4 refactors (we are only 4 group members)
* Using a issue tracker
* Remarkably beautiful visualization of DIY coverage using markdown

## Onboarding experience

### Did it build and run as documented?
We were able to run the unchanged project after some difficulties with java versions, which was not documented.
We had to ignore/remove some failing tests after adding our own changes.

The first project we found was quite old so we decided to go with a more modern project to possibly get an actual PR accepted.

After adding our DIY coverage and trying to move to Java 11, we had lots of issues, and using IntelliJ did not help due to their caching and overriding maven settings. In the end we ported our DIY coverage to Java 8 and started running maven through the command-line instead of an IDE.

## Complexity

1. What are your results for five complex functions?
   * Did all methods (tools vs. manual count) get the same result?
   * Are the results clear?
2. Are the functions just complex, or also long?
3. What is the purpose of the functions?
4. Are exceptions taken into account in the given measurements?
5. Is the documentation clear w.r.t. all the possible outcomes?

### LinkedTreeMap#rebalance

#### CCN
* Lizard CCN: 22
* David CCN: 20 + 21 = 21
* Markus CCN: 21 + 1 = 22
#### Complexity
Complex and long.
#### Purpose
Rebalances an AVL tree
#### Exceptions
Exceptions were not present in the function.
#### Documentation
Documentation is clear what all possible outcomes are

### JsonReader#nextNonWhitespace

#### CCN
* Lizard CCN: 16
* David CCN: 16 + 1 = 17
* Francis CCN: 16
#### Complexity
Complex and long.
#### Purpose
Returns the next character in the stream that is neither whitespace nor a part of a comment.
#### Exceptions
Exceptions were not present in the function.
#### Documentation
Documentation is short but clear what all the possible outcomes

### JsonReader#skipValue

#### CCN
* Lizard CCN: 19
* Ebrar CCN: 18 + 1 = 19
* Francis CCN: 18 + 1 = 19
#### Complexity
Complex and long.
#### Purpose
Skips the next value recursively. This method is intended for use when the JSON token stream contains unrecognized or unhandled values.
#### Exceptions
Exceptions were not present in the function.
#### Documentation
Documentation is clear what all the possible outcomes.

### JsonReader#nextUnquotedValue

#### CCN
* Lizard CCN: 24
* Markus CCN: 23 + 1 = 24
* Ebrar CCN: 23 + 1 = 24
#### Complexity
Mostly long but a bit complex.
#### Purpose
Extracts the next unquoted string from a buffer ending when it reaches a delimiter.
#### Exceptions
IOException gets thrown when an error occurs, however no specific cases.
#### Documentation
The documentation is short but clear (It is a private function).

## Refactoring

All refactored functions live in the refactor branch.

* [ISO8601Utils.java](https://github.com/group-15-dd2480/Assignment-3/blob/6767e0c24dc9e2bfc23e8f79f9ae4b51521552c3/gson/src/main/java/com/google/gson/internal/bind/util/ISO8601Utils.java)
* [LinkedTreeMap.java](https://github.com/group-15-dd2480/Assignment-3/blob/6767e0c24dc9e2bfc23e8f79f9ae4b51521552c3/gson/src/main/java/com/google/gson/internal/LinkedTreeMap.java)
* [JsonReader.java](https://github.com/group-15-dd2480/Assignment-3/blob/6767e0c24dc9e2bfc23e8f79f9ae4b51521552c3/gson/src/main/java/com/google/gson/stream/JsonReader.java)

### Plans

* **ISO8601Utils#parse** - Segment the function into multiple parts, moving the parsing of time part, and timezone part, into their own functions. Should lower CC significantly for little drawback, a bit of unwrapping return values but better than a massive function. In the end the complexity went from 30 to 8 as reported by lizard, with the split functions having complexities of 11 and 15.

* **LinkedTreeMap#rebalance** - The complexity of the function was high, and so it felt appropriate to split the function into separate parts. It was identified that the first two branches in the main if-statement were similar, and so an effort to make this a separate helper function was made. This caused some issues with differents checks, and so these two cases was instead split into two different helper function. This reduced the complexity of the main function from 22 to 12. The two helper functions added each had a complexity of just 6.

* **JsonReader#skipValue** - The function’s CCN was high so splitting into different functions might help instead of using case for each condition.  We can handle start tokens and end tokens seperately into two functions like handleStartToken and handleEndToken. Additionally, we can group all other cases into handleValueToken, which further simplified the main function. The complexity of the main function will be reduced significantly while the helper functions each have small complexity, making the logic easier to follow and modify. 19 CCN to 5 CCN.

* **JsonReader#nextUnquotedValue** The function’s CCN was high because of several if statements but also a long switch case containing several cases. It is most likely like this because it is the most efficient way. A simple way to reduce the CCN however is splitting the function by moving the switch case into its own function to divide the CCN between the functions. This does not necessarily make it more efficient, rather more inefficient, but it reduces the amount of high CCN functions. 

#### **ISO8601Utils#parse** refactor

https://github.com/group-15-dd2480/Assignment-3/commit/88d20184d74f6d5d9af85a9e04e64b67927605f0

#### **LinkedTreeMap#rebalance** refactor

https://github.com/group-15-dd2480/Assignment-3/commit/fa4e7cfba61a3866319545ff13355d06e8abf457

#### **JsonReader#skipValue** refactor

https://github.com/group-15-dd2480/Assignment-3/commit/f04c9ebe62e678c7e78471be64f99d15ba17ad03

#### **JsonReader#nextUnquotedValue** refactor

https://github.com/group-15-dd2480/Assignment-3/commit/619252af3eee764fc07b9803c8bdff9b96d09a25

## Coverage

### Tools

Using JaCoCo was a bit of a pain to start with due to lacking documentation, and the suggested changes to our pom did not work, specifically using `@{argLine}` for `surefire-maven-plugin`, we had to change this to `${argLine}`. Where to add the execution steps was not very clear either.

Once we got it set up through, it worked without fail for the rest of the project.

### Your own coverage tool

[Coverage Branch](https://github.com/group-15-dd2480/Assignment-3/tree/coverage)
[Coverage File](https://github.com/group-15-dd2480/Assignment-3/blob/cfc130fb9bbcb27467a0407bb65839ad71300183/gson/src/main/java/com/google/gson/Coverage.java)

We can instrument any single line in the source code, so we can meansure coverage anywhere where you can put a statement. So for example we dont support single-line ifs, but we can support multi-line ternary operators.

We instrument the lines where we call `Coverage.sample`, we do not compute the spans between the lines.

We output the results as a markdown file for easier viewing. The results seem to be consistent with JaCoCo.

## Coverage improvement

All test improvements for coverage are located in the [Test Branch](https://github.com/group-15-dd2480/Assignment-3/tree/test)

* Ebrar
    * https://github.com/group-15-dd2480/Assignment-3/commit/81b234ca70856c7adbc7e38a4e269a7de5aa7786
        * 1 missed -> 0 missed
    * https://github.com/group-15-dd2480/Assignment-3/commit/94186cc77e630b218b06d6f09db24e51fb253db8
        * 2 missed -> 0 missed
* Francis
    * https://github.com/group-15-dd2480/Assignment-3/commit/8312ae1ef194c0dc71e3fd9fee934f4597432b7b
        * 2 missed -> 1 missed
    * https://github.com/group-15-dd2480/Assignment-3/commit/9fcc8da14bc3cd167b7c958fb844a16633acc60a
        * 1 missed -> 0 missed
* David
    * https://github.com/group-15-dd2480/Assignment-3/commit/37015c2a34207d7a39b17386d0786beb918842a6
        * 3 missed -> 2 missed
    * https://github.com/group-15-dd2480/Assignment-3/commit/416aade52ce66961d86927f4e3d3a59c68982dba
        * missed IoException -> no missed IoException
* Markus
    * https://github.com/group-15-dd2480/Assignment-3/commit/8e6ef6c3d8c608593e4e5d33942c097467a41040
        * 1 missed -> 0 missed
    * https://github.com/group-15-dd2480/Assignment-3/commit/aa526eef9627c3ef97e66f11c7d0bb0365770821
        * 1 missed -> 0 missed

## Self-assessment: Way of working

### Current state according to the Essence standard:
We are currently in Stage 3: In Use in Essence standards.

We have regressed from assignments 1 and 2, we were in stage 5 in assignment 1, and stage 4 in assignment 2. This was mostly due to the shorter deadline and crunching at the end, where we regressed while working under stress.

### Was the self-assessment unanimous? Any doubts about certain items?
We are unanimous on our self-assessment and how we have done so far.

### How have you improved so far?
We had successful onboarding experience while choosing our project, but then realized the project had other issues to address while working on the tests part. So we improved our way of working and refactored some tests along the way.

### How can we improve further
We could improve how we work under stress, due to picking a large project, we ended up having to do a lot of crunching in the end which made us regress in how we handler the essence parts.

## Overall experience

### What are your main take-aways from this project? What did you learn?
Selecting a project to work on should be a detailed and thoughtful process.

We should carefully evaluate the project's feasibility, scope, and potential roadblocks beforehand to save time and effort later
