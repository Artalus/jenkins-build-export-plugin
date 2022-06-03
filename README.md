# jenkins-build-export

## Introduction

This plugin sends HTTP POST request to a configured URL after each completed build in Jenkins.
Said requests contain JSON tree representing pipeline steps (`FlowExecution` in terms of Pipeline API).
You can then handle these requests to your liking, exporting them further to SQL database or analysis tools, or simply spamming to `#jenkins` channel in your messenger of choice.

> ⚠️ NOTE: only Pipeline jobs and their builds are currently supported.
PRs for Freestyle job support are warmly welcome.

## Building plugin

This plugin is not part of a Jenkins plugin suite.
You will need to build `plugin.hpi` and import it to your jenkins manually.
I tried to make it as painless for people not experienced in Java, as a person not experienced in Java (myself) could.

If you already know how to handle Maven to build Jenkins plugins and have Maven configured at hand - have fun, I haven't changed anything to mess with it (I hope).

If you don't have experience with Maven, I made some Docker files for the ease of use.
First you will have to startup the Docker container to contain Maven in isolated environment:
```
docker-compose up -d
```
This will mount current directory with plugin sources and create `.m2/` directory containing Maven cache that can be reused between container restarts.

Now you can build the plugin in the freshly started `maven` container:
```
docker-compose exec maven mvn compile
```
`compile` is a target that instructs Maven to download dependencies, build plugin sources and put results to `target/` directory.
Once the compilation is complete you need to pack the built plugin into `.hpi` archive that can be imported to Jenkins:
```
docker-compose exec maven mvn hpi:hpi
```
The resulting file will be placed in `target/` directory.
Now it can be installed to Jenkins (see [Managing Plugins -> From the web UI](https://www.jenkins.io/doc/book/managing/plugins/#from-the-web-ui) docs for details).

If you wish to test how the plugin behaves before fiddling with your own Jenkins, you can startup a temporary Jenkins instance with it installed:
```
docker-compose exec maven mvn hpi:run -Dhost=0.0.0.0
```
This will launch Jenkins on http://localhost:8080, where you can configure the plugin, run some jobs and see how it works.

## Getting started

Go to Jenkins configuration, find section `Build Export` and provide a URL to which send POSTs.
If you simply want to see what is going on and how the payload looks, you can use any echo server or global webhook service to display request contents (see https://webhook.site for example).

After the URL is provided, the plugin will POST about any newly finished builds.
The process is automatic, you do not need to edit your pipelines.

If you want to "export" old pre-existing builds in the same manner, you will have to do it manually.
It is done using `PipelineProcessor` class and its static `doMagic` method, which accepts a `WorkflowRun` object describing your finished build.
You can use Jenkins Script Console or another Pipeline job for that.

> TODO: make sure example works

Example:
```groovy
// do not blindly run examples from the interwebz in Script Console
// without understanding what they do! :E
import artalus.plugins.buildexport.PipelineProcessor
def JOBS = 'ci/build-cpp/'

Jenkins.instance.getAllItems(Job)
.findAll { it.fullName.startsWith(B) }
.collect { it.builds }
.flatten()
.findAll { ! it.isBuilding() }
.each { PipelineProcessor.doMagic(it) }
```
This will find all jobs in folder `ci/build-cpp/`, iterate over their builds, and run export procedure for each.
Be aware that it is not an intended/optimized way to use plugin, and will take time for jobs with large history.

## Contributing

Refer to Jenkins [contribution guidelines](https://github.com/jenkinsci/.github/blob/master/CONTRIBUTING.md)

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)
