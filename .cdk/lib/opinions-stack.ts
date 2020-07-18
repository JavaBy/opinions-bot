import * as cdk from '@aws-cdk/core';
import {Duration} from '@aws-cdk/core';
import {OpinionsStackProps} from './OpinionsStackProps';
import dynamodb = require('@aws-cdk/aws-dynamodb');
import lambda = require('@aws-cdk/aws-lambda');
import apigateway = require('@aws-cdk/aws-apigateway');

export class OpinionsStack extends cdk.Stack {
	constructor(scope: cdk.Construct, id: string, props: OpinionsStackProps) {
		super(scope, id, props);

		const votestTable = new dynamodb.Table(this, 'opinions-votes', {
			tableName: 'opinions-votes',
			partitionKey: {name: 'id', type: dynamodb.AttributeType.STRING},
			billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
		});
		const youtubeChannelsWhitelistTable = new dynamodb.Table(this, 'opinions-youtube-channels-whitelist', {
			tableName: 'opinions-youtube-channels-whitelist',
			partitionKey: {name: 'channelId', type: dynamodb.AttributeType.STRING},
			billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
		});
		const kotlinMentionsTable = new dynamodb.Table(this, 'opinions-kotlin-mentions', {
			tableName: 'opinions-kotlin-mentions',
			partitionKey: {name: 'chatId', type: dynamodb.AttributeType.STRING},
			billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
		});

		const lambdaFunctionWebhook = new lambda.Function(this, 'opinions-webhook', {
			functionName: 'opinions-webhook',
			runtime: lambda.Runtime.JAVA_11,
			timeout: Duration.seconds(30),
			memorySize: 512,
			code: lambda.Code.fromAsset('../build/libs/opinions-bot-all.jar'),
			handler: 'by.jprof.telegram.opinions.Handler',
			environment: {
				'LOG_THRESHOLD': 'DEBUG',
				'TELEGRAM_BOT_TOKEN': props.telegramToken,
				'YOUTUBE_API_TOKEN': props.youtubeToken,
				'TABLE_VOTES': votestTable.tableName,
				'TABLE_YOUTUBE_CHANNELS_WHITELIST': youtubeChannelsWhitelistTable.tableName,
				'TABLE_KOTLIN_MENTIONS': kotlinMentionsTable.tableName,
				'KOTLIN_MENTIONS_COOLDOWN_MS': Math.floor(Duration.days(1 / 2).toMilliseconds()).toString(),
			}
		});

		votestTable.grantReadWriteData(lambdaFunctionWebhook);
		youtubeChannelsWhitelistTable.grantReadData(lambdaFunctionWebhook);
		kotlinMentionsTable.grantReadWriteData(lambdaFunctionWebhook);

		const api = new apigateway.RestApi(this, 'opinions-bot', {
			restApiName: 'opinions-bot',
			cloudWatchRole: false,
			endpointTypes: [apigateway.EndpointType.REGIONAL],
			deployOptions: {
				loggingLevel: apigateway.MethodLoggingLevel.INFO,
				dataTraceEnabled: true,
				metricsEnabled: true,
				tracingEnabled: true,
			},
		});

		api.root.addResource(props.telegramToken.replace(':', '_')).addMethod('POST', new apigateway.LambdaIntegration(lambdaFunctionWebhook));
	}
}
