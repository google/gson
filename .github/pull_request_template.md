<!--
    Thank you for your contribution!
    Please see the contributing guide: https://github.com/google/.github/blob/master/CONTRIBUTING.md

    Keep in mind that Gson is in maintenance mode. If you want to add a new feature, please first search for existing GitHub issues, or create a new one to discuss the feature and get feedback.
-->

### Purpose
<!-- Describe the purpose of this pull request, for example which new feature it adds or which bug it fixes -->
<!-- If this pull request closes a GitHub issue, please write "Closes #<issue>", for example "Closes #123" -->


### Description
<!-- If necessary provide more information, for example relevant implementation details or corner cases which are not covered yet -->
<!-- If there are related issues or pull requests, link to them by referencing their number, for example "pull request #123" -->



### Checklist
<!-- The following checklist is mainly intended for yourself to verify that you did not miss anything -->

- [ ] New code follows the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [ ] If necessary, new public API validates arguments, for example rejects `null`
- [ ] New public API has Javadoc
    - [ ] Javadoc uses `@since $next-version$`  
      (`$next-version$` is a special placeholder which is automatically replaced during release)
- [ ] If necessary, new unit tests have been added  
  - [ ] Assertions in unit tests use [Truth](https://truth.dev/), see existing tests
  - [ ] No JUnit 3 features are used (such as extending class `TestCase`)
  - [ ] If this pull request fixes a bug, a new test was added for a situation which failed previously and is now fixed
- [ ] `mvn clean verify javadoc:jar` passes without errors
