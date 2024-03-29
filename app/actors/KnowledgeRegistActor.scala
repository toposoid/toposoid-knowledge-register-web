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
import com.ideal.linked.common.DeploymentConverter.conf
import com.ideal.linked.toposoid.common.ToposoidUtils
import com.ideal.linked.toposoid.knowledgebase.featurevector.model.RegistContentResult
import com.ideal.linked.toposoid.knowledgebase.regist.model.{ImageReference, Knowledge, KnowledgeForImage, KnowledgeSentenceSet, PropositionRelation, Reference}
import com.ideal.linked.toposoid.protocol.model.parser.{KnowledgeForParser, KnowledgeSentenceSetForParser}
import com.ideal.linked.toposoid.sentence.transformer.neo4j.Sentence2Neo4jTransformer
import com.ideal.linked.toposoid.vectorizer.FeatureVectorizer
import scala.util.{Failure, Success, Try}
import com.typesafe.scalalogging.LazyLogging
import io.jvm.uuid.UUID
import play.api.libs.json.Json

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
    /*
    case RegistKnowledgeUsingSentenceActor(knowledgeList:List[Knowledge]) => {
      try {
        val propositionIds = (1 to knowledgeList.size).map(x => UUID.random.toString).toList
        Sentence2Neo4jTransformer.createGraphAuto(propositionIds, knowledgeList)
        FeatureVectorizer.createVector(propositionIds, knowledgeList)
      } catch {
        case e: Exception => {
          logger.error(e.toString, e)
        }
      }
      sender() ! "OK "
    }
    */
    case RegistKnowledgeUsingSentenceSetActor(knowledgeSentenceSet:KnowledgeSentenceSet) => {
      try {
        val propositionId =UUID.random.toString
        val knowledgeForParserPremise:List[KnowledgeForParser] = knowledgeSentenceSet.premiseList.map(KnowledgeForParser(propositionId, UUID.random.toString, _))
        val knowledgeForParserClaim:List[KnowledgeForParser] = knowledgeSentenceSet.claimList.map(KnowledgeForParser(propositionId, UUID.random.toString, _))
        val knowledgeSentenceSetForParser = KnowledgeSentenceSetForParser(
          registKnowledgeImages(knowledgeForParserPremise),
          knowledgeSentenceSet.premiseLogicRelation,
          registKnowledgeImages(knowledgeForParserClaim),
          knowledgeSentenceSet.claimLogicRelation)

        Sentence2Neo4jTransformer.createGraph(knowledgeSentenceSetForParser)
        FeatureVectorizer.createVector(knowledgeSentenceSetForParser)
      } catch {
        case e: Exception => {
          logger.error(e.toString, e)
        }
      }
      sender() ! "OK "
    }
  }

  private def registKnowledgeImages(knowledgeForParsers:List[KnowledgeForParser]):List[KnowledgeForParser] = Try {

    knowledgeForParsers.foldLeft(List.empty[KnowledgeForParser]){
      (acc, x) => {
        val knowledgeForImages:List[KnowledgeForImage] = x.knowledge.knowledgeForImages.map(y => {
          val imageFeatureId = UUID.random.toString
          val json: String = Json.toJson(KnowledgeForImage(imageFeatureId, y.imageReference)).toString()
          val knowledgeForImageJson: String = ToposoidUtils.callComponent(json,
              conf.getString("TOPOSOID_CONTENTS_ADMIN_HOST"),
              conf.getString("TOPOSOID_CONTENTS_ADMIN_PORT"),
            "registImage")
          val registContentResult:RegistContentResult = Json.parse(knowledgeForImageJson).as[RegistContentResult]
          if(registContentResult.statusInfo.status.equals("ERROR")) throw new Exception(registContentResult.statusInfo.message)
          registContentResult.knowledgeForImage
        })
        val knowledge = Knowledge(sentence = x.knowledge.sentence,
          lang = x.knowledge.lang, extentInfoJson = x.knowledge.extentInfoJson,
          isNegativeSentence = x.knowledge.isNegativeSentence, knowledgeForImages)
        acc :+ KnowledgeForParser(x.propositionId, x.sentenceId, knowledge)
      }
    }
  } match {
    case Success(s) => s
    case Failure(e) => throw e
  }

}
