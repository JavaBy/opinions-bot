import * as cdk from '@aws-cdk/core';
import {Duration} from '@aws-cdk/core';
import {OpinionsStackProps} from './OpinionsStackProps';
import lambda = require('@aws-cdk/aws-lambda');
import apigateway = require('@aws-cdk/aws-apigateway');

export class OpinionsStack extends cdk.Stack {
	constructor(scope: cdk.Construct, id: string, props: OpinionsStackProps) {
		super(scope, id, props);

		const lambdaFunctionWebhook = new lambda.Function(this, 'opinions-webhook', {
			runtime: lambda.Runtime.JAVA_11,
			timeout: Duration.seconds(30),
			memorySize: 512,
			code: lambda.Code.fromAsset('../build/libs/opinions-bot-all.jar'),
			handler: 'by.jprof.telegram.opinions.Handler',
			environment: {
				'LOG_THRESHOLD': 'DEBUG',
				'TELEGRAM_BOT_TOKEN': props.token
			}
		});

		const api = new apigateway.RestApi(this, 'opinions-bot');

		api.root.addResource(props.token.replace(':', '_')).addMethod('POST', new apigateway.LambdaIntegration(lambdaFunctionWebhook));
	}
}
