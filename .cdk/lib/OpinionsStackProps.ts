import * as cdk from '@aws-cdk/core';

export interface OpinionsStackProps extends cdk.StackProps {
	readonly token: string;
}
