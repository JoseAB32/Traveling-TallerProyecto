export class Translation {
  id!: number;
  entityType!: string;
  entityId!: number;
  fieldName!: string;
  language!: string;
  translatedText!: string;
}

export class TranslationPageResponse {
  content: Translation[] = [];
  page!: number;
  size!: number;
  totalElements!: number;
  totalPages!: number;
  hasNext!: boolean;
}

export class TranslationFilters {
  entityType?: string;
  language?: string;
  fieldName?: string;
  entityId?: number | null;
  page?: number;
  size?: number;
}

export class UpdateTranslationRequest {
  translatedText!: string;
}