/*
 * Copyright 2021 Linked Ideal LLC.[https://linked-ideal.com/]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package actors

import akka.actor.{Actor, Props}
import com.ideal.linked.toposoid.knowledgebase.regist.model.{Knowledge, KnowledgeSentenceSet}
import com.ideal.linked.toposoid.sentence.transformer.neo4j.Sentence2Neo4jTransformer
import com.typesafe.scalalogging.LazyLogging
import io.jvm.uuid.UUID

object RegistKnowledgeActor {
  def props = Props[RegistKnowledgeActor]
  case class RegistKnowledgeUsingSentenceActor(knowledgeList:List[Knowledge])
  case class RegistKnowledgeUsingSentenceSetActor(knowledgeSentenceSet:KnowledgeSentenceSet)
}

/**
 * This module runs an asynchronous batch process.
 * The main implementation of this module is the execution process that
 * converts the result of predicate argument structure analysis of sentences into a graph database.
 */
class RegistKnowledgeActor extends Actor with LazyLogging {

  import RegistKnowledgeActor._

  /**
   *　This function analyzes the requested json with a predicate argument structure and then registers it in the graph database.
   * @return
   */
  def receive = {
    case RegistKnowledgeUsingSentenceActor(knowledgeList:List[Knowledge]) => {
      try {
        Sentence2Neo4jTransformer.createGraphAuto((1 to knowledgeList.size).map(x => UUID.random.toString).toList, knowledgeList)
      } catch {
        case e: Exception => {
          logger.error(e.toString, e)
        }
      }
      sender() ! "OK "
    }
    case RegistKnowledgeUsingSentenceSetActor(knowledgeSentenceSet:KnowledgeSentenceSet) => {
      try {
        Sentence2Neo4jTransformer.createGraph(UUID.random.toString, knowledgeSentenceSet)
      } catch {
        case e: Exception => {
          logger.error(e.toString, e)
        }
      }
      sender() ! "OK "
    }

  }
}
