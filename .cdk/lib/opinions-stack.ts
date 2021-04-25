import * as cdk from '@aws-cdk/core';
import {Duration} from '@aws-cdk/core';
import {OpinionsStackProps} from './OpinionsStackProps';
import dynamodb = require('@aws-cdk/aws-dynamodb');
import lambda = require('@aws-cdk/aws-lambda');
import apigateway = require('@aws-cdk/aws-apigateway');
import events = require('@aws-cdk/aws-events');
import targets = require('@aws-cdk/aws-events-targets');

export class OpinionsStack extends cdk.Stack {
    constructor(scope: cdk.Construct, id: string, props: OpinionsStackProps) {
        super(scope, id, props);

        const votesTable = new dynamodb.Table(this, 'opinions-votes', {
            tableName: 'opinions-votes',
            partitionKey: {name: 'id', type: dynamodb.AttributeType.STRING},
            billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
        });
        const keyboardsTable = new dynamodb.Table(this, 'opinions-keyboards', {
            tableName: 'opinions-keyboards',
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
        const newsQueueJavaTable = new dynamodb.Table(this, 'news-queue', {
            tableName: 'news-queue',
            partitionKey: {name: 'kind', type: dynamodb.AttributeType.STRING},
            sortKey: {name: 'queuedAt', type: dynamodb.AttributeType.STRING},
            billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
        });
        const chatsJavaTable = new dynamodb.Table(this, 'chats', {
            tableName: 'chats',
            partitionKey: {name: 'event', type: dynamodb.AttributeType.STRING},
            sortKey: {name: 'chatId', type: dynamodb.AttributeType.STRING},
            billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
        });

        const lambdaFunctionWebhook = new lambda.Function(this, 'opinions-webhook', {
            functionName: 'opinions-webhook',
            runtime: lambda.Runtime.JAVA_11,
            timeout: Duration.seconds(30),
            memorySize: 1024,
            code: lambda.Code.fromAsset('../webhook/build/libs/opinions-bot-webhook-all.jar'),
            handler: 'by.jprof.telegram.opinions.webhook.Handler',
            environment: {
                'LOG_THRESHOLD': 'DEBUG',
                'TELEGRAM_BOT_TOKEN': props.telegramToken,
                'YOUTUBE_API_TOKEN': props.youtubeToken,
                'TABLE_VOTES': votesTable.tableName,
                'TABLE_KEYBOARDS': keyboardsTable.tableName,
                'TABLE_YOUTUBE_CHANNELS_WHITELIST': youtubeChannelsWhitelistTable.tableName,
                'TABLE_KOTLIN_MENTIONS': kotlinMentionsTable.tableName
            },
        });

        const lambdaFunctionInsideJava = new lambda.Function(this, 'opinions-inside-java-lambda', {
            functionName: 'opinions-inside-java-lambda',
            runtime: lambda.Runtime.JAVA_11,
            timeout: Duration.seconds(30),
            memorySize: 1024,
            code: lambda.Code.fromAsset('../inside-java/build/libs/opinions-bot-inside-java-all.jar'),
            handler: 'by.jprof.telegram.opinions.insidejava.Handler',
            environment: {
                'LOG_THRESHOLD': 'DEBUG',
                'TELEGRAM_BOT_TOKEN': props.telegramToken,
                'YOUTUBE_API_TOKEN': props.youtubeToken,
                'TABLE_NEWS_QUEUE': newsQueueJavaTable.tableName,
                'TABLE_CHATS': chatsJavaTable.tableName,
            },
        });

        votesTable.grantReadWriteData(lambdaFunctionWebhook);
        keyboardsTable.grantReadData(lambdaFunctionWebhook);
        youtubeChannelsWhitelistTable.grantReadData(lambdaFunctionWebhook);
        kotlinMentionsTable.grantReadWriteData(lambdaFunctionWebhook);
        newsQueueJavaTable.grantReadWriteData(lambdaFunctionInsideJava)
        chatsJavaTable.grantReadWriteData(lambdaFunctionInsideJava)

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

        const rule = new events.Rule(this, 'opinions-inside-java-rule', {
            ruleName: 'opinions-inside-java-rule',
            schedule: events.Schedule.expression('cron(0/5 * ? * * *)')
        })

        rule.addTarget(new targets.LambdaFunction(lambdaFunctionInsideJava));
    }
}
