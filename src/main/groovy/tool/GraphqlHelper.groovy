package tool

import io.aexp.nodes.graphql.Arguments
import io.aexp.nodes.graphql.GraphQLRequestEntity
import io.aexp.nodes.graphql.GraphQLResponseEntity
import io.aexp.nodes.graphql.GraphQLTemplate
import io.aexp.nodes.graphql.Variable

import java.awt.image.SampleModel

/**
 * Created by Peter.Yang on 2021/2/4.
 */
class GraphqlHelper {
    static void main(String[] args) {
        GraphQLTemplate graphQLTemplate = new GraphQLTemplate();

        GraphQLRequestEntity requestEntity = GraphQLRequestEntity.Builder()
                .url("http://record.ruianva.cn/v1/graphql")
                .variables(new Variable<>("timeFormat", "MM/dd/yyyy"))
                .scalars(BigDecimal.class)
                .request(SampleModel.class)
                .build();
        GraphQLResponseEntity<SampleModel> responseEntity = graphQLTemplate.query(requestEntity, SampleModel.class);
    }
}
