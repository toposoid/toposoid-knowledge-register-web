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

package controllers

import akka.actor.ActorSystem
import com.ideal.linked.common.DeploymentConverter.conf
import com.ideal.linked.toposoid.common.mq.{KnowledgeRegistrationForManual, MqUtils}
import com.ideal.linked.toposoid.common.{TRANSVERSAL_STATE, ToposoidUtils, TransversalState}
import com.ideal.linked.toposoid.knowledgebase.nlp.model.{SingleSentence, SurfaceInfo}
import com.ideal.linked.toposoid.knowledgebase.regist.model.{Knowledge, KnowledgeSentenceSet}
import com.typesafe.scalalogging.LazyLogging

import javax.inject._
import play.api.libs.json.{Json, OWrites, Reads}
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

case class DetectedLanguage(lang:String)
object DetectedLanguage {
  implicit val jsonWrites: OWrites[DetectedLanguage] = Json.writes[DetectedLanguage]
  implicit val jsonReads: Reads[DetectedLanguage] = Json.reads[DetectedLanguage]
}

/**
 * This module is a WEB API for converting sentences into a knowledge graph and registering them in a graph database.
 * @param system
 * @param cc
 * @param ec
 */
@Singleton
class HomeController @Inject()(system: ActorSystem, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) with LazyLogging{

  def registerForManual()  = Action(parse.json) { request =>
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE .str).get).as[TransversalState]
    try{
      val json = request.body
      val knowledgeSentenceSet: KnowledgeSentenceSet = Json.parse(json.toString).as[KnowledgeSentenceSet]
      val resKnowledgeSentenceSet: String =  ToposoidUtils.callComponent(Json.toJson(knowledgeSentenceSet).toString(), conf.getString("TOPOSOID_LANGUAGE_DETECTOR_HOST"), conf.getString("TOPOSOID_LANGUAGE_DETECTOR_PORT"), "detectLanguages", transversalState)
      val knowledgeRegistrationForManual = KnowledgeRegistrationForManual(knowledgeSentenceSet = Json.parse(resKnowledgeSentenceSet).as[KnowledgeSentenceSet], transversalState = transversalState)
      val jsonStr = Json.toJson(knowledgeRegistrationForManual).toString()
      MqUtils.publishMessage(jsonStr, conf.getString("TOPOSOID_MQ_HOST"), conf.getString("TOPOSOID_MQ_PORT"), conf.getString("TOPOSOID_MQ_KNOWLEDGE_REGISTER_QUENE"))
      logger.info(ToposoidUtils.formatMessageForLogger("Registration completed", transversalState.userId))
      Ok(Json.obj("status" ->"Ok", "message" -> ""))
    }catch{
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString(),transversalState.userId), e)
        BadRequest(Json.obj("status" ->"Error", "message" -> e.toString()))
      }
    }
  }

  def split() = Action(parse.json) { request =>
    val transversalState = Json.parse(request.headers.get(TRANSVERSAL_STATE.str).get).as[TransversalState]
    try {
      val json = request.body
      val singleSentence: SingleSentence = Json.parse(json.toString).as[SingleSentence]
      val resDetectedLanguage = ToposoidUtils.callComponent(Json.toJson(singleSentence).toString(), conf.getString("TOPOSOID_LANGUAGE_DETECTOR_HOST"), conf.getString("TOPOSOID_LANGUAGE_DETECTOR_PORT"), "detectLanguage", transversalState)
      val detectedLanguage =  Json.parse(resDetectedLanguage).as[DetectedLanguage]
      val surfaceInfoList:List[SurfaceInfo] = detectedLanguage.lang match {
        case "ja_JP" => {
          val  res = ToposoidUtils.callComponent(json.toString() ,conf.getString("TOPOSOID_SENTENCE_PARSER_JP_WEB_HOST"), conf.getString("TOPOSOID_SENTENCE_PARSER_JP_WEB_PORT"), "split", transversalState)
          Json.parse(res.toString).as[List[SurfaceInfo]]
        }
        case "en_US" => {
          val res = ToposoidUtils.callComponent(json.toString(), conf.getString("TOPOSOID_SENTENCE_PARSER_EN_WEB_HOST"), conf.getString("TOPOSOID_SENTENCE_PARSER_EN_WEB_PORT"), "split", transversalState)
          Json.parse(res.toString).as[List[SurfaceInfo]]
        }
        case _ => {
          logger.warn(ToposoidUtils.formatMessageForLogger("This language is not covered by Toposoid." + singleSentence , transversalState.userId))
          List.empty[SurfaceInfo]
        }
      }
      logger.info(ToposoidUtils.formatMessageForLogger("Splitting completed." + surfaceInfoList.mkString(","), transversalState.userId))
      Ok(Json.toJson(surfaceInfoList)).as(JSON)
    } catch {
      case e: Exception => {
        logger.error(ToposoidUtils.formatMessageForLogger(e.toString, transversalState.userId), e)
        BadRequest(Json.obj("status" -> "Error", "message" -> e.toString()))
      }
    }
  }


}
