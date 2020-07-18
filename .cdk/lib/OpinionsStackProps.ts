import * as cdk from '@aws-cdk/core';

export interface OpinionsStackProps extends cdk.StackProps {
	readonly telegramToken: string;
	readonly youtubeToken: string;
}
