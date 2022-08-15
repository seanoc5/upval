package configs

{
   id = "${foundry.destination.APP}_${foundry.FEATURE_NAME}_QPL"
   stages = [
      {
         id = "ece2b7eb-2916-4aff-91f2-8664563c825b"
         script = "function (request, response, ctx) {\n \n  var q = request.getFirstParam('q')\n  request.putSingleParam('q', \"ta_type:entity &&\" + q)\n}"
         shareState = true
         type = "javascript-query"
         skip = false
         label = "Only Return Entity Documents"
         condition = "request.hasParam(\"entityOnly\") && request.getFirstParam(\"entityOnly\").equals(\"true\");\n// This stage is used to the make the results only entity documents. The ${foundry.destination.APP}_TYPEAHEAD_DC_entity_QPF will send a entityOnly=true"
         secretSourceStageId = "ece2b7eb-2916-4aff-91f2-8664563c825b"
      },
      {
         id = "2e25eeaa-ac2b-4e42-8603-39dea784d9ff"
         script = "function (request, response, ctx) {\n \n  var q = request.getFirstParam('q')\n  request.putSingleParam('q', \"ta_type:history && \" + q)\n}"
         shareState = true
         type = "javascript-query"
         skip = false
         label = "Only Return History Documents"
         condition = "request.hasParam(\"historyOnly\") && request.getFirstParam(\"historyOnly\").equals(\"true\");\n// This stage is used to the make the results only history documents. The ${foundry.destination.APP}_TYPEAHEAD_DC_history_QPF will send a parameter historyOnly=true"
         secretSourceStageId = "2e25eeaa-ac2b-4e42-8603-39dea784d9ff"
      },
      {
         id = "9615c4bf-bd84-4c61-9ff3-2c07c73dc582"
         numRecommendations = 10
         numSignals = 100
         aggrType = "click@doc_id,filters,query"
         boostId = "id"
         boostingMethod = "query-param"
         boostingParam = "boost"
         queryParams = [
            {
               key = "qf"
               value = "query_t"
            },
            {
               key = "pf"
               value = "query_t^50"
            },
            {
               key = "pf"
               value = "query_t~3^20"
            },
            {
               key = "pf2"
               value = "query_t^20"
            },
            {
               key = "pf2"
               value = "query_t~3^10"
            },
            {
               key = "pf3"
               value = "query_t^10"
            },
            {
               key = "pf3"
               value = "query_t~3^5"
            },
            {
               key = "boost"
               value = "map(query({!field f=query_s v=$q}),0,0,120)"
            },
            {
               key = "mm"
               value = "50%"
            },
            {
               key = "defType"
               value = "edismax"
            },
            {
               key = "sort"
               value = "score desc weight_d desc"
            },
            {
               key = "fq"
               value = "weight_d:[* TO *]"
            }
         ]
         rollupField = "doc_id_s"
         rollupWeightField = "weight_d"
         weightExpression = "math:log(weight_d + 1) + 10 * math:log(score+1)"
         rollupWeightStrategy = "max"
         queryParamToBoost = "q"
         includeEnrichedQuery = false
         type = "recommendation"
         skip = false
         secretSourceStageId = "9615c4bf-bd84-4c61-9ff3-2c07c73dc582"
      }
  ]
}
