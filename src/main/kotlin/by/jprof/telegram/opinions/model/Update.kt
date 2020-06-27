package by.jprof.telegram.opinions.model

import com.github.insanusmokrassar.TelegramBotAPI.types.update.abstracts.Update as NewBotApiUpdate
import by.dev.madhead.telek.model.Update as MadHeadLibUpdate

data class Update(val oldUpdate: MadHeadLibUpdate?, val newUpdate : NewBotApiUpdate?)