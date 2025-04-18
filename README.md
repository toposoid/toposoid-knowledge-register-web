# toposoid-knowledge-register-web
This is a WEB API that works as a microservice within the toposoid project.
Toposoid is a knowledge base construction platform.(see [Toposoid　Root Project](https://github.com/toposoid/toposoid.git))
This Microservice registers the results of predicate argument structure analysis of Japanese natural sentences in a graph database.

[![Unit Test And Build Image Action](https://github.com/toposoid/toposoid-knowledge-register-web/actions/workflows/action.yml/badge.svg?branch=main)](https://github.com/toposoid/toposoid-knowledge-register-web/actions/workflows/action.yml)

* input


| Japanse | English |
| ------------- | ------------- | 
|<img width="748" src="https://github.com/toposoid/toposoid-knowledge-register-web/assets/82787843/17ec2390-fe21-41d4-a028-0ecf0760a58d">|<img width="747" src="https://github.com/toposoid/toposoid-knowledge-register-web/assets/82787843/e95b194d-49d2-49b8-958c-2ce6e30f5243">|

* result
<img width="1597" src="https://github.com/toposoid/toposoid-knowledge-register-web/assets/82787843/b18a893f-97ab-49fc-aa32-bb5d322274c0">


## Requirements
* Docker version 20.10.x, or later
* docker-compose version 1.22.x
* The following microservices must be running
  * toposoid/toposoid-sentence-parser-japanese-web
  * toposoid/toposoid-sentence-parser-english-web
  * toposoid/toposoid-common-nlp-japanese-web
  * toposoid/toposoid-common-nlp-english-web
  * toposoid-common-image-recognition-web
  * toposoid/toposoid-contents-admin-web
  * toposoid/data-accessor-weaviate-web
  * semitechnologies/weaviate
  * neo4j


## Recommended Environment For Standalone
* Required: at least 16GB of RAM
* Required: at least 46G of HDD(Total required Docker Image size)
* Please understand that since we are dealing with large models such as LLM, the Dockerfile size is large and the required machine SPEC is high.

## Setup For Standalone
```bash
docker-compose up 
```
* It takes more than 20 minutes to pull the Docker image for the first time.

## Usage
```bash
#-----------------------------------------
#Case1 A Simple Sentence in Japanese
#-----------------------------------------
curl -X POST -H "Content-Type: application/json" -d '{
    "premiseList": [],
    "premiseLogicRelation": [],
    "claimList": [
        {
            "sentence": "案ずるより産むが易し",
            "lang": "ja_JP",
            "extentInfoJson": "{}",
            "isNegativeSentence": false,
            "knowledgeForImages": []
        }
    ],
    "claimLogicRelation": []
}' http://localhost:9002/regist

#-----------------------------------------
#Case2 Multiple Sentences in Japanese
#-----------------------------------------
curl -X POST -H "Content-Type: application/json" -d {
    "premiseList": [
        {
            "sentence": "これはテストの前提1です。",
            "lang": "ja_JP",
            "extentInfoJson": "{}",
            "isNegativeSentence": false,
            "knowledgeForImages": []
        },
        {
            "sentence": "これはテストの前提2です。",
            "lang": "ja_JP",
            "extentInfoJson": "{}",
            "isNegativeSentence": false,
            "knowledgeForImages": []
        }
    ],
    "premiseLogicRelation": [
        {
            "operator": "AND",
            "sourceIndex": 0,
            "destinationIndex": 1
        }
    ],
    "claimList": [
        {
            "sentence": "これはテストの主張1です。",
            "lang": "ja_JP",
            "extentInfoJson": "{}",
            "isNegativeSentence": false,
            "knowledgeForImages": []
        },
        {
            "sentence": "これはテストの主張2です。",
            "lang": "ja_JP",
            "extentInfoJson": "{}",
            "isNegativeSentence": false,
            "knowledgeForImages": []
        }
    ],
    "claimLogicRelation": [
        {
            "operator": "OR",
            "sourceIndex": 0,
            "destinationIndex": 1
        }
    ]
}' http://localhost:9002/regist

#-----------------------------------------
#Case3 A Simple Sentence in English
#-----------------------------------------
curl -X POST -H "Content-Type: application/json" -d '{
    "premiseList": [],
    "premiseLogicRelation": [],
    "claimList": [
        {
            "sentence": "Our life is our art.",
            "lang": "en_US",
            "extentInfoJson": "{}",
            "isNegativeSentence": false,
            "knowledgeForImages": []
        }
    ],
    "claimLogicRelation": []
}' http://localhost:9002/regist

#-----------------------------------------
#Case4 Multiple Sentences in English
#-----------------------------------------
curl -X POST -H "Content-Type: application/json" -d '{
    "premiseList": [
        {
            "sentence": "This is premise-1.",
            "lang": "en_US",
            "extentInfoJson": "{}",
            "isNegativeSentence": false,
            "knowledgeForImages": []
        },
        {
            "sentence": "This is premise-2.",
            "lang": "en_US",
            "extentInfoJson": "{}",
            "isNegativeSentence": false,
            "knowledgeForImages": []
        }
    ],
    "premiseLogicRelation": [
        {
            "operator": "AND",
            "sourceIndex": 0,
            "destinationIndex": 1
        }
    ],
    "claimList": [
        {
            "sentence": "This is claim-1.",
            "lang": "en_US",
            "extentInfoJson": "{}",
            "isNegativeSentence": false,
            "knowledgeForImages": []
        },
        {
            "sentence": "This is claim-2.",
            "lang": "en_US",
            "extentInfoJson": "{}",
            "isNegativeSentence": false,
            "knowledgeForImages": []
        }
    ],
    "claimLogicRelation": [
        {
            "operator": "OR",
            "sourceIndex": 0,
            "destinationIndex": 1
        }
    ]
}' http://localhost:9002/regist
```
* Images are registered when KnowledgeForImages is set.

## License
toposoid/toposoid-knowledge-register-web is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

## Author
* Makoto Kubodera([Linked Ideal LLC.](https://linked-ideal.com/))

Thank you!
