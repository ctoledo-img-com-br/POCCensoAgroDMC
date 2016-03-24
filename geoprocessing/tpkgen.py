# -*- coding: utf-8 -*-
# ---------------------------------------------------------------------------
# tpkgen.py
# Created on: 2016-03-11
# Author: Carlos Eduardo Toledo @ Imagem Geosistemas
# Description: Geração e empacotamento de Tile Cache para áreas de interesse
# ---------------------------------------------------------------------------

# Import arcpy module
import arcpy
import ntpath
from datetime import datetime

# Parameters:
PROJETO_MAPA_BASE = arcpy.GetParameterAsText(0)
AOI_DATASET = arcpy.GetParameterAsText(1)
AOI_FILTER = arcpy.GetParameterAsText(2)
AOI_FIELD_NAME = arcpy.GetParameterAsText(3)
OUTPUT_DIR = arcpy.GetParameterAsText(4) 

SCRATCH_WKS = arcpy.env.scratchWorkspace
AOI_SELECTION = "{0}\\tpkgen_AOI_SELECTION".format(SCRATCH_WKS)

BASEMAP_SCALES = arcpy.GetParameterAsText(5)
MIN_SCALE = "4622324,434309"
MAX_SCALE = "4513,988705"

arcpy.AddMessage("\nExecutando seleção do filtro {0} no dataset {1}".format(AOI_FILTER, AOI_DATASET))
arcpy.Delete_management(AOI_SELECTION)
arcpy.Select_analysis(AOI_DATASET, AOI_SELECTION, AOI_FILTER)

DT_INICIO = datetime.now()
arcpy.AddMessage("\nGerando o Tile Cache para o projeto de mapa: {0}".format(PROJETO_MAPA_BASE))

NOME_TILE_CACHE = ntpath.basename((PROJETO_MAPA_BASE.split("."))[0])

arcpy.ManageTileCache_management(OUTPUT_DIR, "RECREATE_ALL_TILES", NOME_TILE_CACHE, PROJETO_MAPA_BASE, "ARCGISONLINE_SCHEME", "", BASEMAP_SCALES, "", "", MIN_SCALE, MAX_SCALE)

DT_FIM = datetime.now()
arcpy.AddMessage("Finalizado tile cache em {0} (horas:minutos:segundos)".format((DT_FIM - DT_INICIO)))

with arcpy.da.SearchCursor(AOI_SELECTION, ['SHAPE@', AOI_FIELD_NAME]) as cursor:
    for row in cursor:
		DT_INICIO = datetime.now()
	
		AOI = row[0]
		NOME_AOI = row[1]
		
		arcpy.AddMessage("\nEnpacotando o Tile Cache da Área de Interesse: {0}".format(NOME_AOI))

		AOI_SHAPE =  "{0}\\tpkgen_AOI".format(SCRATCH_WKS) 
		arcpy.Delete_management(AOI_SHAPE)
		arcpy.CopyFeatures_management(AOI, AOI_SHAPE)

		TILE_CACHE_PATH = "{0}\\{1}\\Layers".format(OUTPUT_DIR, NOME_TILE_CACHE) 
		TILE_CACHE_PACKAGE = NOME_AOI

		arcpy.ExportTileCache_management(TILE_CACHE_PATH, OUTPUT_DIR, TILE_CACHE_PACKAGE, "TILE_PACKAGE", "COMPACT", BASEMAP_SCALES, AOI_SHAPE)
		
		DT_FIM = datetime.now()
		arcpy.AddMessage("Finalizado empacotamento em {0} (horas:minutos:segundos)".format((DT_FIM - DT_INICIO)))

arcpy.AddMessage("\n")		
		
arcpy.Delete_management(AOI_SELECTION)


