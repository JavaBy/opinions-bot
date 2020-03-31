#!/usr/bin/env node

import 'source-map-support/register';
import * as cdk from '@aws-cdk/core';
import {OpinionsStack} from '../lib/opinions-stack';

if (process.env.TELEGRAM_BOT_TOKEN == null) {
	throw new Error('Undefined TELEGRAM_BOT_TOKEN')
}

const app = new cdk.App();
new OpinionsStack(
		app,
		'OpinionsStack',
		{
			token: process.env.TELEGRAM_BOT_TOKEN,
			env: {
				region: 'eu-west-2'
			}
		}
);
