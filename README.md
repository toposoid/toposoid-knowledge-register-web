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
can.

* KnowledgeSentenceSet

| name | type                     | explanation   |
| ------------- |--------------------------|---------------|
| premiseList | List[Knowledge]          | see Knowledge |
| premiseLogicRelation | List[PropositionRelation] | see PropositionRelation   |
| claimList | List[Knowledge] | see Knowledge |
| claimLogicRelation | List[PropositionRelation]                   | see PropositionRelation |

* Knowledge

| name               | type                    | explanation                                              |
|--------------------|-------------------------|----------------------------------------------------------|
| sentence           | String                  | sentence                                                 |
| lang               | String                  | ja_JP or en_US                                           |
| extentInfoJson     | String                  | Additional information can be registered in Json format. |
| isNegativeSentence | Boolean                 | Currently fixed to False                                 |
| knowledgeForImages | List[KnowledgeForImage] | see KnowledgeForImage                                    |
| knowledgeForTables | List[KnowledgeForTable] | see KnowledgeForTable                                    |

* PropositionRelation

| name | type   | explanation                                                                       |
| ------------- |--------|-----------------------------------------------------------------------------------|
| operator | String | 'AND' 'OR'                                                                        |
| sourceIndex | Int    | Source for binary operation. Specified by index of premiseList or claimList.      |
| destinationIndex | Int    | Destination for binary operation. Specified by index of premiseList or claimList. |

* KnowledgeForImage

| name           | type    | explanation                       |
|----------------|---------|-----------------------------------|
| id             | String  | Unique id that identifies the image |
| imageReference | ImageReference  | see ImageReference  |

* ImageReference

| name      | type      | explanation               |
|-----------|-----------|---------------------------|
| reference | Reference | see Reference             |
| x         | Int       | x coordinate of image TOP |
| y         | Int       | y coordinate of image TOP |
| width     | Int       | Image width               |
| height    | Int       | Image height              |

* Reference

| name    | type      | explanation                                                               |
|---------|-----------|---------------------------------------------------------------------------|
| url | String | url                                                                       |
| surface        | String    | Words in the text linked to images                                        |
| surfaceIndex        | Int       | Index in the sentence                                                     |
| isWholeSentence   | Boolean   | True if the image is associated with the entire sentence, otherwise False |
| originalUrlOrReference  | String       | original url                                                              |

* KnowledgeForTable

| name           | type           | explanation                         |
|----------------|----------------|-------------------------------------|
| id             | String         | Unique id that identifies the image |
| TableReference | TableReference | see TableReference                  |

* TableReference

| name      | type      | explanation               |
|-----------|-----------|---------------------------|
| reference | Reference | see Reference             |



Try accessing http://localhost:7474 in your browser.
You will be able to see the data you registered from the API.
as follows
<img width="1597" src="https://github.com/toposoid/toposoid-knowledge-register-web/assets/82787843/b18a893f-97ab-49fc-aa32-bb5d322274c0">


## Note
* This microservice uses 9002 as the default port.
* If you want to run in a remote environment or a virtual environment, change PRIVATE_IP_ADDRESS in docker-compose.yml according to your environment.
* The memory allocated to Neo4J can be adjusted with NEO4J_dbms_memory_heap_max__size in docker-compose.yml.  
* The Node's Information In A Graph Database

| name | content(Japanse) |content(English)|
| ------------- | ------------- | ------------- |
| nodeId | 文章の文節を識別するID | ID that identifies the morpheme in the text |
| propositionId | 命題としての文章集合を識別するID |　Same as Japanese |
| currentId | 文章の何番目の文節かを識別するID | ID of token parsed by [CoreNLP](https://stanfordnlp.github.io/CoreNLP/) |
| parentId | 当該の文節が係っている文節のID | ID of token.head parsed by [CoreNLP](https://stanfordnlp.github.io/CoreNLP/)　|
| isMainSection | 文末を表すフラグ ture/false | Same as Japanese |
| surface | 文節の表層 |token.text parsed by [CoreNLP](https://stanfordnlp.github.io/CoreNLP/)　 |
| normalizedName | 文節の正規化表現（KNPのfeatureの正規化代表表記参照） |token.lemma parsed by [CoreNLP](https://stanfordnlp.github.io/CoreNLP/)　|
| dependType　| 親の文節との関係　依存関係:D、並列関係:P | - |
| caseType | 文節の格情報（KNPのfeatureの係参照） | [Stanford Dependencies](https://downloads.cs.stanford.edu/nlp/software/dependencies_manual.pdf)|
| namedEntity | 固有表現（KNPのfeatureのNE参照） | Mainly NER calculated from [spacy's](https://spacy.io/) [en_core_web_lg](https://spacy.io/models/en#en_core_web_lg) model |
| rangeExpressions | 範囲表現 | Same as Japanese |
| categories | カテゴリ（KNPのfeatureのカテゴリ参照） | - |
| domains | ドメイン（KNPのfeatureのドメイン参照） | - |
| isDenialWord | 否定表現を表すフラグ true/false (KNPのfeatureの否定参照) |　Negatives analyzed by [Core NLP's Depedency](https://downloads.cs.stanford.edu/nlp/software/dependencies_manual.pdf)　|
| isConditionalConnection | 条件節及びそれに類する節を表すフラグ（KNPのfeatureの条件節候補参照） | Conditional clause analyzed by [Depedency of CoreNLP](https://downloads.cs.stanford.edu/nlp/software/dependencies_manual.pdf) |
| normalizedNameYomi　| 文節の正規化表現の読み仮名 | - |
| surfaceYomi | 文節の表層の読み仮名 | - |
| modalityType | モダリティ（KNPのfeatureのモダリティ参照） | - |
| logicType | (KNPの並列タイプ参照、その他包含関係はIMPという種別もあり) | - |
| nodeType | com.ideal.linked.toposoid.common.SentenceType |　Same as Japanese |
| extentText | 拡張領域 |Same as Japanese |

The Edge's Information In A Graph Database

| name | content(Japanese) | content(English) |
| ------------- | ------------- | ------------- |
| sourceId | 文節間の関係で係受けの子を識別するID | ID of token parsed by [CoreNLP](https://stanfordnlp.github.io/CoreNLP/) |
| destinationId　| 文節間の関係で係受けの親を識別するID | ID of token.head parsed by [CoreNLP](https://stanfordnlp.github.io/CoreNLP/)　|
| caseStr　| 文節間の関係（格構造 etc） | [Stanford Dependencies](https://downloads.cs.stanford.edu/nlp/software/dependencies_manual.pdf) |
| dependType | KnowledgeBaseNodeのdependType参照 | - |
| logicType　| KnowledgeBaseNodeのlogicType参照 | - |

## License
toposoid/toposoid-knowledge-register-web is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

## Author
* Makoto Kubodera([Linked Ideal LLC.](https://linked-ideal.com/))

Thank you!
