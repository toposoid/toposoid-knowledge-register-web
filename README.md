# toposoid-knowledge-register-web
This is a WEB API that works as a microservice within the toposoid project.
Toposoid is a knowledge base construction platform.(see [Toposoid　Root Project](https://github.com/toposoid/toposoid.git))
This Microservice registers the results of predicate argument structure analysis of Japanese natural sentences in a graph database.

* input
<img width="678" alt="2021-09-21 16 28 08" src="https://user-images.githubusercontent.com/82787843/134147540-b20fb585-da7c-4c51-a0e7-81ccd524cfc4.png">

* result
<img width="1280" alt="2021-09-21" src="https://user-images.githubusercontent.com/82787843/134151546-8110e0e5-f07d-4771-a78e-8e19c4090cd3.png">

It's very simple!!

## Requirements
* Docker version 20.10.x, or later
* docker-compose version 1.22.x

## Memory requirements
* Required: at least 8GB of RAM (The maximum heap memory size of the JVM is set to 6G (Application: 4G, Neo4J: 2G))
* Required: 10G or higher　of HDD

## Setup
```bash
docker-compose up -d
```
It takes more than 20 minutes to pull the Docker image for the first time.
## Usage
```bash
curl -X POST -H "Content-Type: application/json" -d '{"sentences":["案ずるより産むが易し"]}' http://localhost:9002/regist
```
Try accessing http://localhost:7474 in your browser.
You will be able to see the data you registered from the API.
as follows
<img width="1287" alt="2021-09-21 19 05 58" src="https://user-images.githubusercontent.com/82787843/134152542-cd3e5255-5bcd-4920-88d8-4aeff88842d8.png">



## Note
* This microservice uses 9002 as the default port.
* If you want to run in a remote environment or a virtual environment, change PRIVATE_IP_ADDRESS in docker-compose.yml according to your environment.
* The memory allocated to Neo4J can be adjusted with NEO4J_dbms_memory_heap_max__size in docker-compose.yml.  
* The Node's Information In A Graph Database
| name | content |
| ------------- | ------------- |
| nodeId | 文章の文節を識別するID |
| propositionId | 命題としての文章集合を識別するID |
| currentId | 文章の何番目の文節かを識別するID |
| parentId | 当該の文節が係っている文節のID |
| isMainSection | 文末を表すフラグ ture/false |
| surface | 文節の表層 |
| normalizedName | 文節の正規化表現（KNPのfeatureの正規化代表表記参照） |
| dependType　| 親の文節との関係　依存関係:D、並列関係:P |
| caseType | 文節の格情報（KNPのfeatureの係参照） |
| namedEntity | 固有表現（KNPのfeatureのNE参照） |
| rangeExpressions | 範囲表現 |
| categories | カテゴリ（KNPのfeatureのカテゴリ参照） |
| domains | ドメイン（KNPのfeatureのドメイン参照） |
| isDenial | 否定表現を表すフラグ true/false (KNPのfeatureの否定参照) |
| isConditionalConnection | 条件節及びそれに類する節を表すフラグ（KNPのfeatureの条件節候補参照） |
| normalizedNameYomi　| 文節の正規化表現の読み仮名 |
| surfaceYomi | 文節の表層の読み仮名 |
| modalityType | モダリティ（KNPのfeatureのモダリティ参照） |
| logicType | (KNPの並列タイプ参照、その他包含関係はIMPという種別もあり) |
| nodeType | com.ideal.linked.toposoid.common.SentenceType |
| extentText | 拡張領域 |

The Edge's Information In A Graph Database
| name | content |
| ------------- | ------------- |
| sourceId | 文節間の関係で係受けの子を識別するID |
| destinationId　| 文節間の関係で係受けの親を識別するID |
| caseStr　| 文節間の関係（格構造 etc） |
| dependType | KnowledgeBaseNodeのdependType参照 |
| logicType　| KnowledgeBaseNodeのlogicType参照 |

## License
toposoid/toposoid-knowledge-register-web is Open Source software released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0.html).

## Author
* Makoto Kubodera([Linked Ideal LLC.](https://linked-ideal.com/))

Thank you!
