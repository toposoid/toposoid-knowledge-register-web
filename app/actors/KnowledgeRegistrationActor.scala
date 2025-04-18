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
import com.ideal.linked.toposoid.common.mq.KnowledgeRegistrationForManual
import com.ideal.linked.toposoid.common.{ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.featurevector.model.RegistContentResult
import com.ideal.linked.toposoid.knowledgebase.regist.model.{ImageReference, Knowledge, KnowledgeForImage, KnowledgeSentenceSet, PropositionRelation, Reference}
import com.ideal.linked.toposoid.protocol.model.parser.{KnowledgeForParser, KnowledgeSentenceSetForParser}
import com.ideal.linked.toposoid.sentence.transformer.neo4j.Sentence2Neo4jTransformer
import com.ideal.linked.toposoid.vectorizer.FeatureVectorizer

import scala.util.{Failure, Success, Try}
import com.typesafe.scalalogging.LazyLogging
import io.jvm.uuid.UUID
import play.api.libs.json.Json

object KnowledgeRegistrationActor {
  def props = Props[KnowledgeRegistrationActor]
  //case class RegistKnowledgeUsingSentenceActor(knowledgeList:List[Knowledge])
  case class RegisterKnowledgeForManualActor(knowledgeSentenceSet:KnowledgeSentenceSet, transversalState:TransversalState)
  case class RegisterKnowledgeForDocumentActor(knowledgeSentenceSet:KnowledgeSentenceSet, transversalState:TransversalState)
}

/**
 * This module runs an asynchronous batch process.
 * The main implementation of this module is the execution process that
 * converts the result of predicate argument structure analysis of sentences into a graph database.
 */
class KnowledgeRegistrationActor extends Actor with LazyLogging {

  import KnowledgeRegistrationActor._

  /**
   *　This function analyzes the requested json with a predicate argument structure and then registers it in the graph database.
   * @return
   */
  def receive = {
    case RegisterKnowledgeForManualActor(knowledgeSentenceSet:KnowledgeSentenceSet, transversalState:TransversalState) => {
      try {
        val knowledgeRegistrationForManual = KnowledgeRegistrationForManual(knowledgeSentenceSet = knowledgeSentenceSet, transversalState = transversalState)
        val jsonStr = Json.toJson(knowledgeRegistrationForManual).toString()
        ToposoidUtils.publishMessage(jsonStr,conf.getString("TOPOSOID_MQ_HOST"), conf.getString("TOPOSOID_MQ_PORT"), conf.getString("TOPOSOID_MQ_KNOWLEDGE_REGISTER_QUENE"))
      } catch {
        case e: Exception => {
          logger.error(e.toString, e)
        }
      }
      sender() ! "OK "
    }
  }

  /*
  private def registKnowledgeImages(knowledgeForParsers:List[KnowledgeForParser], transversalState:TransversalState):List[KnowledgeForParser] = Try {

    knowledgeForParsers.foldLeft(List.empty[KnowledgeForParser]){
      (acc, x) => {
        val knowledgeForImages:List[KnowledgeForImage] = x.knowledge.knowledgeForImages.map(y => {
          val imageFeatureId = UUID.random.toString
          val json: String = Json.toJson(KnowledgeForImage(imageFeatureId, y.imageReference)).toString()
          val knowledgeForImageJson: String = ToposoidUtils.callComponent(json,
              conf.getString("TOPOSOID_CONTENTS_ADMIN_HOST"),
              conf.getString("TOPOSOID_CONTENTS_ADMIN_PORT"),
            "registImage", transversalState)
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
  */
}
