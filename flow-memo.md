This is the job being tested:
```groovy
stages = [
    'a': 3,
    'b': 5,
    'c': 10,
]
parallel stages.collectEntries { a, b ->
    ["$a": {
        stage("stage $a: sleep for $b") {
            node() { sleep b }
        }
    }]
}
```
This is how its printed depth-first "stack" (reversed) looks from commit `ddae1574`:
```ruby
      {{{ #8 (from 3) Branch: c @ 10:35:32
          +BodyInvocationAction
         {{{ #13 (from 8) @ 10:35:32
             +LogStorageAction
             +ArgumentsActionImpl
            {{{ #14 (from 13) stage c: sleep for 5 @ 10:35:32
                +BodyInvocationAction
               {{{ #17 (from 14) @ 10:35:32
                   +LogStorageAction
                   +QueueItemActionImpl
                   +WorkspaceActionImpl - master
                  {{{ #24 (from 17) @ 10:35:35
                      +BodyInvocationAction - node acquired after 3.10 s
                     --> #26 StepAtomNode @ 10:35:35
                         +ArgumentsActionImpl
                         +LogStorageAction
                  }}} 24 (took 5.03 s)
                      +BodyInvocationAction
               }}} 17 (took 8.14 s)
            }}} 14 (took 8.23 s)
                +BodyInvocationAction
         }}} 13 (took 8.25 s)
      }}} 8 (took 8.47 s)
          +BodyInvocationAction
      {{{ #7 (from 3) Branch: b @ 10:35:32
          +BodyInvocationAction
         {{{ #11 (from 7) @ 10:35:32
             +LogStorageAction
             +ArgumentsActionImpl
            {{{ #12 (from 11) stage b: sleep for 4 @ 10:35:32
                +BodyInvocationAction
               {{{ #16 (from 12) @ 10:35:32
                   +LogStorageAction
                   +QueueItemActionImpl
                   +WorkspaceActionImpl - master
                  {{{ #19 (from 16) @ 10:35:32
                      +BodyInvocationAction - node acquired after 0.05 s
                     --> #21 StepAtomNode @ 10:35:32
                         +ArgumentsActionImpl
                         +LogStorageAction
                  }}} 19 (took 4.05 s)
                      +BodyInvocationAction
               }}} 16 (took 4.12 s)
            }}} 12 (took 4.21 s)
                +BodyInvocationAction
         }}} 11 (took 4.23 s)
      }}} 7 (took 4.44 s)
          +BodyInvocationAction
--> #2 FlowStartNode @ 10:35:31
   {{{ #3 (from 2) @ 10:35:32
       +LogStorageAction
      {{{ #6 (from 3) Branch: a @ 10:35:32
          +BodyInvocationAction
         {{{ #9 (from 6) @ 10:35:32
             +LogStorageAction
             +ArgumentsActionImpl
            {{{ #10 (from 9) stage a: sleep for 3 @ 10:35:32
                +BodyInvocationAction
               {{{ #15 (from 10) @ 10:35:32
                   +LogStorageAction
                   +QueueItemActionImpl
                   +WorkspaceActionImpl - master
                  {{{ #18 (from 15) @ 10:35:32
                      +BodyInvocationAction - node acquired after 0.07 s
                     --> #20 StepAtomNode @ 10:35:32
                         +ArgumentsActionImpl
                         +LogStorageAction
                  }}} 18 (took 3.04 s)
                      +BodyInvocationAction
               }}} 15 (took 3.13 s)
            }}} 10 (took 3.22 s)
                +BodyInvocationAction
         }}} 9 (took 3.27 s)
      }}} 6 (took 3.46 s)
          +BodyInvocationAction
   }}} 3 (took 8.54 s)
--> #40 FlowEndNode @ 10:35:40
```

And this is how GSon treats ArrayList<NodeData> it (non-reversed) (yamlized for line economy):
```yaml
- id: 40
  parent: 0
  depth: 0
  children: []
  content:
    result:
      name: SUCCESS
      ordinal: 0
      color: BLUE
      completeBuild: true
    startId: '2'
    parentIds:
    - '39'
    id: '40'
- id: 39
  parent: 2
  depth: 1
  children: []
  content:
    startId: '3'
    parentIds:
    - '28'
    - '33'
    - '38'
    id: '39'
- id: 28
  parent: 3
  depth: 2
  children: []
  content:
    startId: '6'
    parentIds:
    - '27'
    id: '28'
- id: 27
  parent: 6
  depth: 3
  children: []
  content:
    startId: '9'
    parentIds:
    - '25'
    id: '27'
- id: 25
  parent: 9
  depth: 4
  children: []
  content:
    startId: '10'
    parentIds:
    - '23'
    id: '25'
- id: 23
  parent: 10
  depth: 5
  children: []
  content:
    startId: '15'
    parentIds:
    - '22'
    id: '23'
- id: 22
  parent: 15
  depth: 6
  children: []
  content:
    startId: '19'
    parentIds:
    - '21'
    id: '22'
- id: 21
  parent: 19
  depth: 7
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.steps.SleepStep
    parentIds:
    - '19'
    id: '21'
- id: 19
  parent: 15
  depth: 6
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.support.steps.ExecutorStep
    parentIds:
    - '15'
    id: '19'
- id: 15
  parent: 10
  depth: 5
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.support.steps.ExecutorStep
    parentIds:
    - '10'
    id: '15'
- id: 10
  parent: 9
  depth: 4
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.support.steps.StageStep
    parentIds:
    - '9'
    id: '10'
- id: 9
  parent: 6
  depth: 3
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.support.steps.StageStep
    parentIds:
    - '6'
    id: '9'
- id: 6
  parent: 3
  depth: 2
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.cps.steps.ParallelStep
    parentIds:
    - '3'
    id: '6'
- id: 3
  parent: 2
  depth: 1
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.cps.steps.ParallelStep
    parentIds:
    - '2'
    id: '3'
- id: 2
  parent: 0
  depth: 0
  children: []
  content:
    parentIds: []
    id: '2'
- id: 33
  parent: 3
  depth: 2
  children: []
  content:
    startId: '7'
    parentIds:
    - '32'
    id: '33'
- id: 32
  parent: 7
  depth: 3
  children: []
  content:
    startId: '11'
    parentIds:
    - '31'
    id: '32'
- id: 31
  parent: 11
  depth: 4
  children: []
  content:
    startId: '12'
    parentIds:
    - '30'
    id: '31'
- id: 30
  parent: 12
  depth: 5
  children: []
  content:
    startId: '16'
    parentIds:
    - '29'
    id: '30'
- id: 29
  parent: 16
  depth: 6
  children: []
  content:
    startId: '18'
    parentIds:
    - '20'
    id: '29'
- id: 20
  parent: 18
  depth: 7
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.steps.SleepStep
    parentIds:
    - '18'
    id: '20'
- id: 18
  parent: 16
  depth: 6
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.support.steps.ExecutorStep
    parentIds:
    - '16'
    id: '18'
- id: 16
  parent: 12
  depth: 5
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.support.steps.ExecutorStep
    parentIds:
    - '12'
    id: '16'
- id: 12
  parent: 11
  depth: 4
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.support.steps.StageStep
    parentIds:
    - '11'
    id: '12'
- id: 11
  parent: 7
  depth: 3
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.support.steps.StageStep
    parentIds:
    - '7'
    id: '11'
- id: 7
  parent: 3
  depth: 2
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.cps.steps.ParallelStep
    parentIds:
    - '3'
    id: '7'
- id: 38
  parent: 3
  depth: 2
  children: []
  content:
    startId: '8'
    parentIds:
    - '37'
    id: '38'
- id: 37
  parent: 8
  depth: 3
  children: []
  content:
    startId: '13'
    parentIds:
    - '36'
    id: '37'
- id: 36
  parent: 13
  depth: 4
  children: []
  content:
    startId: '14'
    parentIds:
    - '35'
    id: '36'
- id: 35
  parent: 14
  depth: 5
  children: []
  content:
    startId: '17'
    parentIds:
    - '34'
    id: '35'
- id: 34
  parent: 17
  depth: 6
  children: []
  content:
    startId: '24'
    parentIds:
    - '26'
    id: '34'
- id: 26
  parent: 24
  depth: 7
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.steps.SleepStep
    parentIds:
    - '24'
    id: '26'
- id: 24
  parent: 17
  depth: 6
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.support.steps.ExecutorStep
    parentIds:
    - '17'
    id: '24'
- id: 17
  parent: 14
  depth: 5
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.support.steps.ExecutorStep
    parentIds:
    - '14'
    id: '17'
- id: 14
  parent: 13
  depth: 4
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.support.steps.StageStep
    parentIds:
    - '13'
    id: '14'
- id: 13
  parent: 8
  depth: 3
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.support.steps.StageStep
    parentIds:
    - '8'
    id: '13'
- id: 8
  parent: 3
  depth: 2
  children: []
  content:
    descriptorId: org.jenkinsci.plugins.workflow.cps.steps.ParallelStep
    parentIds:
    - '3'
    id: '8'
```
